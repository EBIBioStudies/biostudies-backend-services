package ac.uk.ebi.biostd.common.events

// Rabbit mq exchange @see [AMQP Model Explained](https://www.rabbitmq.com/tutorials/amqp-concepts.html)
const val BIOSTUDIES_EXCHANGE = "biostudies-exchange"

// Used for release warning email notifications.
const val SUBMISSIONS_PUBLISHED_ROUTING_KEY = "bio.submission.published.notification"

// Used when a new submission has been submitted.
const val SUBMISSIONS_ROUTING_KEY = "bio.submission.published"

// Used for security notification as password reset, user created
const val SECURITY_NOTIFICATIONS_ROUTING_KEY = "bio.security.notification"

// Indicates the ui that a submission need to be refreshed
const val SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY = "bio.submission.partials"

// Internal only. Used when a new submission request fail at any stage.
const val SUBMISSIONS_FAILED_REQUEST_ROUTING_KEY = "bio.submission.failed"
