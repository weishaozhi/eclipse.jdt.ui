/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */package org.eclipse.jdt.internal.core.refactoring;import org.eclipse.core.resources.IResource;import org.eclipse.core.resources.ResourcesPlugin;import org.eclipse.core.runtime.IPath;import org.eclipse.core.runtime.IProgressMonitor;import org.eclipse.core.runtime.SubProgressMonitor;import org.eclipse.jdt.core.ICompilationUnit;import org.eclipse.jdt.core.IJavaElement;import org.eclipse.jdt.core.IPackageFragment;import org.eclipse.jdt.core.IPackageFragmentRoot;import org.eclipse.jdt.core.JavaCore;import org.eclipse.jdt.core.JavaModelException;import org.eclipse.jdt.internal.core.refactoring.base.Change;import org.eclipse.jdt.internal.core.refactoring.base.ChangeContext;import org.eclipse.jdt.internal.core.refactoring.base.IChange;import org.eclipse.jdt.internal.core.refactoring.base.RefactoringStatus;
public abstract class AbstractRenameChange extends Change {
	private String fNewName;	private String fOldName;	private IPath fResourcePath;	private IChange fUndoChange;		protected AbstractRenameChange(IPath resourcePath, String oldName, String newName){		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$		Assert.isNotNull(oldName, "old name"); //$NON-NLS-1$				fResourcePath= resourcePath;		fOldName= oldName;		fNewName= newName;	}		private IResource getResource(){		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);	}		public IJavaElement getCorrespondingJavaElement() {		return JavaCore.create(getResource());	}	public final IChange getUndoChange() {		return fUndoChange;	}			protected abstract IChange createUndoChange() throws JavaModelException;		protected abstract void doRename(IProgressMonitor pm) throws Exception;	public final void perform(ChangeContext context, IProgressMonitor pm) throws JavaModelException {		try{			pm.beginTask(RefactoringCoreMessages.getString("AbstractRenameChange.Renaming"), 1); //$NON-NLS-1$			if (isActive()){				fUndoChange= createUndoChange();				doRename(pm);			} else{				fUndoChange= new NullChange();			}		} catch (Exception e) {			handleException(context, e);			fUndoChange= new NullChange();			setActive(false);		} finally {			pm.done();		}	}	/**	 * Gets the newName.	 * @return Returns a String	 */	protected String getNewName() {		return fNewName;	}	/**	 * Gets the resourcePath.	 * @return Returns a IPath	 */	protected IPath getResourcePath() {		return fResourcePath;	}
	/**	 * Gets the oldName	 * @return Returns a String	 */	protected String getOldName() {		return fOldName;	}		protected static RefactoringStatus checkIfUnsaved(IPackageFragmentRoot root, ChangeContext context, IProgressMonitor pm){		if (root == null)			return null;				if (! root.exists())			return null;				if (root.isArchive())			return null;					if (root.isExternal())			return null;				RefactoringStatus result= new RefactoringStatus();						try {			IJavaElement[] packs= root.getChildren();			if (packs == null || packs.length == 0)				return null;						pm.beginTask("", packs.length); //$NON-NLS-1$			for (int i= 0; i < packs.length; i++) {				result.merge(checkIfUnsaved((IPackageFragment)packs[i], context, new SubProgressMonitor(pm, 1)));			}				pm.done();		} catch (JavaModelException e) {			handleJavaModelException(e, result);		}		return result;	}		protected static RefactoringStatus checkIfUnsaved(IPackageFragment pack, ChangeContext context, IProgressMonitor pm) throws JavaModelException{		ICompilationUnit[] units= pack.getCompilationUnits();		if (units == null || units.length == 0)			return null;				RefactoringStatus result= new RefactoringStatus();				pm.beginTask("", units.length); //$NON-NLS-1$		for (int i= 0; i < units.length; i++) {			pm.subTask("Checking change for:" + pack.getElementName());			checkIfResourceIsUnsaved(units[i], result, context);			pm.worked(1);		}		pm.done();				return result;	}}
