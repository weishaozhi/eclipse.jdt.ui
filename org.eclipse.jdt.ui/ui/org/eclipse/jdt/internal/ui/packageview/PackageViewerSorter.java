/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.packageview;
 
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 *	A sorter to sort the packages in the packages viewer in the following order:
 * 	1st package fragment root order as defined in the class path
 * 	2nd alphabetically
 */
public class PackageViewerSorter extends ViewerSorter { 
	
	// these instance variables serve as a cache for a single sort operation.
	private boolean fIsClassPathSortOrder;
	private IClasspathEntry[] fClassPath;

	public boolean isSorterProperty(Object element, String property) {
		return (IBasicPropertyConstants.P_TEXT.equals(property) || IBasicPropertyConstants.P_IMAGE.equals(property));
	}
				
	public void sort(Viewer v, Object[] property) {
		fClassPath= null;
		
		try {
			if (property.length < 2)
				return;
			
			fIsClassPathSortOrder= isClassPathSortOrder(property);
			super.sort(v, property);
		} finally {
			fClassPath= null;
		}
	}

	boolean isClassPathSortOrder(Object[] property) {
		boolean rootCount= false;
		boolean packageCount= false;
		for (int i= 0; i < property.length; i++) {
			if ((property[i] instanceof IPackageFragmentRoot))
				rootCount= true;
			else if ((property[i] instanceof IPackageFragment))
				packageCount= true;
		}
		return (rootCount || packageCount);
	}
	
	public int compare(Viewer v, Object e1, Object e2) {
		// show resources after Java elements
		// we have to handle both IStorage and IResource separately
		IResource r1= null;
		IResource r2= null;
		if (e1 instanceof IResource)
			r1= (IResource)e1;
		if (e2 instanceof IResource)
			r2= (IResource)e2;
		if (r1 != null && r2 != null)
			return r1.getName().compareToIgnoreCase(r2.getName());
		if (r1 != null && r2 == null)
			return 1;
		if (r1 == null && r2 != null)
			return -1;
	
		IStorage s1= null;
		IStorage s2= null;
		if (e1 instanceof IStorage)
			s1= (IStorage)e1;
		if (e2 instanceof IStorage)
			s2= (IStorage)e2;
		if (s1 != null && s2 != null)
			return s1.getName().compareToIgnoreCase(s2.getName());
		if (s1 != null && s2 == null)
			return 1;
		if (s1 == null && s2 != null)
			return -1;

		if (fIsClassPathSortOrder) {
			int p1= classPathIndex((IJavaElement)e1);
			int p2= classPathIndex((IJavaElement)e2);
			if (p1 < p2)
				return -1;
			if (p1 > p2)
				return 1;
		}
		
		IPackageFragment p1= null;	
		if (e1 instanceof IPackageFragment) {
			p1= (IPackageFragment)e1;
		}
		IPackageFragment p2= null;	
		if (e2 instanceof IPackageFragment) {
			p2= (IPackageFragment)e2;
		}
		if (p1 != null && p2 != null) {
			// compare between non-java package fragment and java package fragment
			try {
				boolean isFolder1= isFolder(p1);
				boolean isFolder2= isFolder(p2);

				if (isFolder1 && !isFolder2) 
					return 1;
				if (!isFolder1 && isFolder2)
					return -1;
			} catch(JavaModelException e) {
			}
		}
			
		String name1= ((IJavaElement)e1).getElementName();
		String name2= ((IJavaElement)e2).getElementName();
		return name1.compareToIgnoreCase(name2);
	}

	boolean isFolder(IPackageFragment p) throws JavaModelException {
		return (!p.hasChildren()) && (p.getNonJavaResources().length >0);
	}

	int classPathIndex(IJavaElement object) {
		IPackageFragmentRoot root= null;
		if (object instanceof IPackageFragment) 
			root= (IPackageFragmentRoot)((IPackageFragment)object).getParent();
		if (object instanceof IPackageFragmentRoot)
			root= (IPackageFragmentRoot)object;
		Assert.isNotNull(root, PackagesMessages.getString("Sorter.expectPackage")); //$NON-NLS-1$
		
		try {
			if (fClassPath == null)
				fClassPath= root.getJavaProject().getResolvedClasspath(true);
		} catch(JavaModelException e) {
			return 0;
		}
		for (int i= 0; fClassPath != null && i < fClassPath.length; i++) {
			if (fClassPath[i].getPath().equals(root.getPath()))
				return i;
		}
		return 0;
	}
}
