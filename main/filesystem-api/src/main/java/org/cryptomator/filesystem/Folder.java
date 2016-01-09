/*******************************************************************************
 * Copyright (c) 2015 Markus Kreusch
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 ******************************************************************************/
package org.cryptomator.filesystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * A {@link Folder} in a {@link FileSystem}.
 * 
 * @author Markus Kreusch
 */
public interface Folder extends Node {

	/**
	 * <p>
	 * Creates a {@link Stream} over all child nodes of this {@code Folder}.
	 * <p>
	 * <b>Note:</b> The {@link Stream} may be lazily populated and thus
	 * {@link IOException IOExceptions} may occurs after this method returned.
	 * In this case implementors should throw a {@link UncheckedIOException}
	 * from any method that produces an {@link IOException}. Thus users should
	 * expect {@link UncheckedIOException UncheckedIOExceptions} when invoking
	 * methods on the returned {@code Stream}.
	 * 
	 * @return the created {@code Stream}
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while initializing the
	 *             stream
	 */
	Stream<? extends Node> children() throws UncheckedIOException;

	/**
	 * <p>
	 * Returns the child {@link Node} in this directory of type {@link File}
	 * with the specified name.
	 * <p>
	 * This operation always returns a {@link File} without checking if the file
	 * exists or is a {@link Folder} instead.
	 */
	File file(String name) throws UncheckedIOException;

	/**
	 * Returns a file by resolving a path relative to this folder.
	 * 
	 * @param path A unix-style path, which is always relative to this folder, no matter if it starts with a slash or not
	 * @return File with the given path relative to this folder
	 */
	default File resolveFile(String relativePath) throws UncheckedIOException {
		return PathResolver.resolveFile(this, relativePath);
	}

	/**
	 * <p>
	 * Returns the child {@link Node} in this directory of type {@link Folder}
	 * with the specified name.
	 * <p>
	 * This operation always returns a {@link Folder} without checking if the
	 * folder exists or is a {@link File} instead.
	 */
	Folder folder(String name) throws UncheckedIOException;

	/**
	 * Returns a folder by resolving a path relative to this folder.
	 * 
	 * @param path A unix-style path, which is always relative to this folder, no matter if it starts with a slash or not
	 * @return Folder with the given path relative to this folder
	 */
	default Folder resolveFolder(String relativePath) throws UncheckedIOException {
		return PathResolver.resolveFolder(this, relativePath);
	}

	/**
	 * Creates the directory including all parent directories, if it doesn't
	 * exist yet. No effect, if folder already exists.
	 * 
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while creating the folder or
	 *             one of its parents
	 */
	void create() throws UncheckedIOException;

	/**
	 * Recusively copies this directory and all its contents to (not into) the
	 * given destination, creating nonexisting parent directories. If the target
	 * exists it is deleted before performing the copy.
	 * 
	 * @param target
	 *            Destination folder. Must not be a descendant of this folder.
	 */
	default void copyTo(Folder target) throws UncheckedIOException {
		Copier.copy(this, target);
	}

	/**
	 * <p>
	 * Deletes the directory including all child elements.
	 * <p>
	 * If the directory does not exist this method does nothing.
	 */
	void delete();

	/**
	 * Moves this directory and its contents to the given destination. If the
	 * target exists it is deleted before performing the move.
	 */
	void moveTo(Folder target);

	/**
	 * @return the result of {@link #children()} filtered to contain only
	 *         {@link File Files}
	 */
	default Stream<? extends File> files() throws UncheckedIOException {
		return children() //
				.filter(File.class::isInstance) //
				.map(File.class::cast);
	}

	/**
	 * @return the result of {@link #children()} filtered to contain only
	 *         {@link Folder Folders}
	 */
	default Stream<? extends Folder> folders() throws UncheckedIOException {
		return children() //
				.filter(Folder.class::isInstance) //
				.map(Folder.class::cast);
	}

	/**
	 * Recursively checks whether this folder or any subfolder contains the
	 * given node.
	 * 
	 * @param node
	 *            Potential child, grandchild, ...
	 * @return <code>true</code> if this folder is an ancestor of the node.
	 */
	default boolean isAncestorOf(Node node) {
		if (!node.parent().isPresent()) {
			return false;
		} else if (node.parent().get().equals(this)) {
			return true;
		} else {
			return this.isAncestorOf(node.parent().get());
		}
	}

	/**
	 * <p>
	 * Sets the creation time of the folder.
	 * <p>
	 * Setting the creation time may not be supported by all {@link FileSystem FileSystems}. If the {@code FileSystem} this {@code Folder} belongs to does not support the
	 * setting the creation time the behavior of this method is unspecified.
	 * 
	 * @param instant the time to set as creation time
	 */
	default void setCreationTime(Instant instant) throws UncheckedIOException {
		throw new UncheckedIOException(new IOException("CreationTime not supported"));
	}

}
