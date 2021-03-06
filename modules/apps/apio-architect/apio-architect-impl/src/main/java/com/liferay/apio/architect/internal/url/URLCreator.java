/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.internal.url;

import static java.lang.String.join;

import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.internal.operation.BatchCreateOperation;
import com.liferay.apio.architect.internal.operation.CreateOperation;
import com.liferay.apio.architect.internal.operation.DeleteOperation;
import com.liferay.apio.architect.internal.operation.RetrieveOperation;
import com.liferay.apio.architect.internal.operation.UpdateOperation;
import com.liferay.apio.architect.internal.pagination.PageType;
import com.liferay.apio.architect.operation.Operation;
import com.liferay.apio.architect.pagination.Page;
import com.liferay.apio.architect.uri.Path;

import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

/**
 * Manages the creation of URLs and has all their necessary information.
 *
 * <p>
 * This class shouldn't be instantiated.
 * </p>
 *
 * @author Alejandro Hernández
 */
public final class URLCreator {

	/**
	 * Returns the absolute version of an application's relative URL.
	 *
	 * @param  applicationURL the application URL
	 * @param  relativeURL the relative URL
	 * @return the absolute URL
	 */
	public static String createAbsoluteURL(
		ApplicationURL applicationURL, String relativeURL) {

		return _buildURL(applicationURL.get(), relativeURL);
	}

	/**
	 * Returns the absolute version of a relative URL.
	 *
	 * @param  serverURL the server URL
	 * @param  relativeURL the relative URL
	 * @return the absolute URL
	 */
	public static String createAbsoluteURL(
		ServerURL serverURL, String relativeURL) {

		return _buildURL(serverURL.get(), relativeURL);
	}

	/**
	 * Returns the URL for a binary resource.
	 *
	 * @param  applicationURL the application URL
	 * @param  binaryId the binary resource's ID
	 * @param  path the resource's {@code com.liferay.apio.architect.uri.Path}
	 * @return the URL for a binary resource
	 */
	public static String createBinaryURL(
		ApplicationURL applicationURL, String binaryId, Path path) {

		return createAbsoluteURL(
			applicationURL, join("/", "b", path.asURI(), binaryId));
	}

	/**
	 * Returns the URL for a collection page.
	 *
	 * @param  collectionURL the collection URL
	 * @param  page the page
	 * @return the collection page URL
	 */
	public static String createCollectionPageURL(
		String collectionURL, Page page, PageType pageType) {

		return UriBuilder.fromUri(
			collectionURL
		).queryParam(
			"page", pageType.getPageNumber(page)
		).queryParam(
			"per_page", page.getItemsPerPage()
		).build(
		).toString();
	}

	/**
	 * Returns the URL for a collection.
	 *
	 * @param  applicationURL the application URL
	 * @param  name the resource's name
	 * @return the collection URL
	 */
	public static String createCollectionURL(
		ApplicationURL applicationURL, String name) {

		return createAbsoluteURL(applicationURL, "/p/" + name);
	}

	/**
	 * Returns the URL for a {@code Form}.
	 *
	 * @param  applicationURL the application URL
	 * @param  form the form
	 * @return the URL for a {@code Form}
	 */
	public static String createFormURL(
		ApplicationURL applicationURL, Form form) {

		return createAbsoluteURL(applicationURL, join("/", "f", form.getId()));
	}

	/**
	 * Returns the URL for a nested collection.
	 *
	 * @param  applicationURL the application URL
	 * @param  path the single resource's {@link Path}
	 * @param  name the nested resource's name
	 * @return the collection URL
	 */
	public static String createNestedCollectionURL(
		ApplicationURL applicationURL, Path path, String name) {

		return createAbsoluteURL(applicationURL, _getReusablePath(path, name));
	}

	/**
	 * Returns an operation's URL.
	 *
	 * @param  applicationURL the application URL
	 * @param  operation the operation
	 * @return the operation's URL
	 */
	public static Optional<String> createOperationURL(
		ApplicationURL applicationURL, Operation operation) {

		Optional<String> optional = operation.getURIOptional();

		return optional.map(
			uri -> {
				if (operation instanceof BatchCreateOperation) {
					return "batch/" + uri;
				}

				if (operation.isCustom()) {
					return _createCustomOperationURL(operation, uri);
				}

				if (operation instanceof CreateOperation) {
					return "p/" + uri;
				}

				if (operation instanceof DeleteOperation) {
					return "p/" + uri;
				}

				if (operation instanceof RetrieveOperation) {
					return "p/" + uri;
				}

				if (operation instanceof UpdateOperation) {
					return "p/" + uri;
				}

				return null;
			}
		).map(
			uri -> createAbsoluteURL(applicationURL, uri)
		);
	}

	/**
	 * Returns the URL of a model's resource.
	 *
	 * @param  applicationURL the application URL
	 * @param  path the resource's {@link Path}
	 * @return the URL for the {@link
	 *         com.liferay.apio.architect.resource.CollectionResource}
	 */
	public static String createSingleURL(
		ApplicationURL applicationURL, Path path) {

		return createAbsoluteURL(applicationURL, "/p/" + path.asURI());
	}

	/**
	 * Returns the {@link Path} from the HTTP servlet request's original URL.
	 *
	 * @return a Path objectSingleModelWriter.java
	 */
	public static Path getPath(String url) {
		String[] serverAndPath = url.split("/[bfp]/");

		if (serverAndPath.length == 2) {
			String fullPath = serverAndPath[1];

			String[] pathComponents = fullPath.split("/");

			String id = pathComponents.length == 1 ? null : pathComponents[1];

			return new Path(pathComponents[0], id);
		}

		return null;
	}

	private static String _buildURL(String baseUrl, String relativeURL) {
		if ((relativeURL == null) || relativeURL.isEmpty()) {
			return null;
		}

		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		if (relativeURL.startsWith("/")) {
			relativeURL = relativeURL.substring(1);
		}

		return join("/", baseUrl, relativeURL);
	}

	private static String _createCustomOperationURL(
		Operation operation, String uri) {

		StringBuilder sb = new StringBuilder();

		sb.append("c/");
		sb.append(uri);
		sb.append("/");
		sb.append(operation.getCustomRoute());

		return sb.toString();
	}

	private static String _getReusablePath(Path path, String name) {
		if (name.equals(path.getName())) {
			return join("/", "p", "r", path.asURI());
		}

		return join("/", "p", path.asURI(), name);
	}

	private URLCreator() {
		throw new UnsupportedOperationException();
	}

}