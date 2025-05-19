package no.nav.arbeidsplassen.importapi.security

// Resulterer i 403 Forbidden
class ForbiddenException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Throwable) : super(msg, cause)
    constructor() : super()
}

// Resulterer i 401 Unauthorized
class UnauthorizedException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Throwable) : super(msg, cause)
    constructor() : super()
}

class NotFoundException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Throwable) : super(msg, cause)
    constructor() : super()
}
