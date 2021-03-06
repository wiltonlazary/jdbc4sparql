/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xenei.jdbc4sparql.impl;

import org.xenei.jdbc4sparql.iface.NamespacedObject;

public class NamespaceImpl implements NamespacedObject {
	private final String namespace;
	private final String localName;
	private final int hashCode;

	protected NamespaceImpl(String namespace, final String localName) {
		this.namespace = namespace;
		this.localName = localName;
		if (!(namespace.endsWith("#") || namespace.endsWith("/"))) {
			namespace += namespace.contains("#") ? "/" : "#";
		}
		hashCode = NamespacedObject.Utils.hashCode(this);
	}

	@Override
	public boolean equals(final Object o) {
		return NamespacedObject.Utils.equals(this, o);
	}

	@Override
	public String getFQName() {
		return namespace + localName;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

}
