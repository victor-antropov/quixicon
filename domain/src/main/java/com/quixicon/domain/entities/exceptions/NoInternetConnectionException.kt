package com.quixicon.domain.entities.exceptions

import java.io.IOException

/**
 * An exception should be thrown when checking internet connection.
 */
class NoInternetConnectionException : IOException("No internet connection")
