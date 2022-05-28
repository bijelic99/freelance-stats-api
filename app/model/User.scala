package model

import org.joda.time.DateTime

case class User(
    id: String,
    username: String,
    password: String,
    email: String,
    firstName: String,
    lastName: String,
    birthDate: DateTime,
    deleted: Boolean
)
