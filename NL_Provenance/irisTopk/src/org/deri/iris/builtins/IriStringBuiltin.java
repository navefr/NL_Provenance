/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.builtins;

import static org.deri.iris.factory.Factory.BASIC;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;

/**
 * <p>
 * Represents the RIF built-in predicate func:iri-string.
 * </p>
 */
public class IriStringBuiltin extends BooleanBuiltin {

	/** The predicate defining this built-in. */
	private static final IPredicate PREDICATE = BASIC.createPredicate(
			"IRI_STRING", 2);

	/**
	 * Creates the built-in for the specified terms.
	 * 
	 * @param terms The terms.
	 * @throws NullPointerException If one of the terms is <code>null</code>.
	 * @throws IllegalArgumentException If the number of terms submitted is not
	 *             2 .
	 */
	public IriStringBuiltin(ITerm... terms) {
		super(PREDICATE, terms);
	}

	@Override
	protected boolean computeResult(ITerm[] terms) {
		// Assuming the IRI is represented by some concrete term.
		if (terms[0] instanceof IConcreteTerm
				&& terms[1] instanceof IStringTerm) {
			IConcreteTerm iri = (IConcreteTerm) terms[0];
			IStringTerm string = (IStringTerm) terms[1];

			return iri.toCanonicalString().equals(string.toCanonicalString());
		}

		return false;
	}

}
