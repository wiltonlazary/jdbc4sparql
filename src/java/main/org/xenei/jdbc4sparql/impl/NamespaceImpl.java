package org.xenei.jdbc4sparql.impl;

import org.xenei.jdbc4sparql.iface.NamespacedObject;

public class NamespaceImpl implements NamespacedObject
{
	private String namespace;
	private String localName;

	protected NamespaceImpl( String namespace, String localName)
	{
		this.namespace = namespace;
		this.localName=localName;
		if (! (namespace.endsWith("#") || namespace.endsWith( "/" )  ))
		{
			namespace+=namespace.contains("#")?"/":"#";
		}
	}
	
	@Override
	public String getNamespace()
	{
		return namespace;
	}

	@Override
	public String getLocalName()
	{
		return localName;
	}

	@Override
	public String getFQName()
	{
		return namespace+localName;
	}

}