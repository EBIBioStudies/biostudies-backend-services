package ebi.ac.uk.util.collections

/**
 * Execute the given lambda is list is not empty.
 *
 * @function lambda function to execute.
 */
fun <A> List<A>.ifNotEmpty(function: (List<A>) -> Unit) = {
    if (this.isNotEmpty()) {
        function(this)
    }
}
