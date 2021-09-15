package ebi.ac.uk.security.exception

class GroupsGroupDoesNotExistsException(
    groupName: String
) : RuntimeException("The group $groupName does not exists")

class GroupsUserDoesNotExistsException(
    userEmail: String
) : RuntimeException("The user $userEmail does not exists")

class GroupsGroupNameMustNotBeNullException : RuntimeException("group name must not be null")

class GroupsGroupDescriptionMustNotBeNullException : RuntimeException("group description must not be null")

class GroupsUserNameMustNotBeNullException : RuntimeException("user name must not be null")
