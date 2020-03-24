package ebi.ac.uk.notifications.exception

class InvalidResponseException : RuntimeException("RT server has given a null response")

class InvalidTicketIdException : RuntimeException("Ticket id not found in the response")
