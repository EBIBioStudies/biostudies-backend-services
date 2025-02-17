package ebi.ac.uk.io.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader

fun BufferedReader.asFlow(): Flow<String> =
    flow {
        var line = readLine()
        while (line != null) {
            emit(line)
            line = readLine()
        }
    }.flowOn(Dispatchers.IO)
