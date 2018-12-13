package ebi.ac.uk.util.web

import java.nio.file.Paths

/**
 * Normalize the given string which represent a path.
 *
 *  /foo -> /foo
 *  //foo -> /foo
 *  foo/ -> /foo
 *  foo/bar -> /foo/bar
 *  foo/bar/../baz -> /foo/baz
 *  foo//bar -> /foo/bar
 */
fun normalize(path: String) = if (path.isBlank()) path else Paths.get("/", path).normalize().toString()
