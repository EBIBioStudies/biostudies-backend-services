FROM azul/zulu-openjdk:11

# Set the user and group for the application
RUN addgroup --system -gid 1001 systemGroup
RUN adduser --system -uid 1002  systemUser
USER systemUser:systemGroup

# Also, there is a clean separation between dependencies and application resources in a Spring Boot fat JAR file,
# and we can use that fact to improve performance. The key is to create layers in the container filesystem.
# The layers are cached both at build time and at runtime (in most runtimes), so we want the most frequently changing
# resources (usually the class and static resources in the application itself) to be layered after the more slowly
# changing resources.
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","ac.uk.ebi.biostd.SubmissionWebApp"]
