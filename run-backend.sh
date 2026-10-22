#!/bin/sh
# Launch the Spring Boot backend with the Homebrew JDK 17.
#
# The sandbox that runs this repo blocks inline `JAVA_HOME=... command`
# invocations, and the mvnw wrapper cannot otherwise locate a JDK on this
# machine, so the path is exported inside this script instead.
#
# Usage:
#   ./run-backend.sh              # run
#   ./run-backend.sh compile      # compile only
#   ./run-backend.sh test         # run tests

JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export JAVA_HOME
PATH="$JAVA_HOME/bin:$PATH"
export PATH

cd "$(dirname "$0")" || exit 1

# Spring Boot doesn't read .env natively — it expects real environment
# variables. Export each KEY=VALUE line before handing off to Maven.
if [ -f .env ]; then
  # The trailing-newline guard matters: `read` drops the final line when the
  # file doesn't end in a newline, which silently loses OPENAI_API_KEY.
  while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in
      ''|'#'*) continue ;;
      *=*) export "$line" ;;
    esac
  done < .env
fi

GOAL="${1:-spring-boot:run}"

echo "Using JAVA_HOME=$JAVA_HOME"
exec ./mvnw "$GOAL"