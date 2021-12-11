package it.lm96.qbrtools.persistence


class PersistenceException : Exception {
    constructor(msg : String) : super(msg)
    constructor(exception: Exception) : super(exception)
    constructor(cause: Throwable) : super(cause)
}