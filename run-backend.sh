#!/bin/sh
# Run the Spring Boot backend with JDK 17 and the variables from .env.
#
# Usage:
#   ./run-backend.sh              # start on :8080
#   ./run-backend.sh compile      # compile only
#   ./run-backend.sh test         # run tests
#   ./run-backend.sh -q compile   # any other Maven args are passed through

cd "$(dirname "$0")" || exit 1

# Prefer the Homebrew JDK 17 on macOS (mvnw can't find a JDK there on its own);
# otherwise keep whatever JAVA_HOME is already set.
BREW_JDK=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
if [ -d "$BREW_JDK" ]; then
  JAVA_HOME=$BREW_JDK
fi
if [ -n "$JAVA_HOME" ]; then
  export JAVA_HOME
  PATH="$JAVA_HOME/bin:$PATH"
  export PATH
fi

# Spring Boot doesn't read .env itself, so export each KEY=VALUE line.
# `|| [ -n "$line" ]` keeps the last line when the file has no trailing newline.
if [ -f .env ]; then
  while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in
      ''|'#'*) continue ;;
      *=*)
        key=${line%%=*}
        value=${line#*=}
        # strip one pair of surrounding quotes: NAME="Some Value"
        case "$value" in
          \"*\") value=${value#\"}; value=${value%\"} ;;
          \'*\') value=${value#\'}; value=${value%\'} ;;
        esac
        export "$key=$value"
        ;;
    esac
  done < .env
fi

echo "Using JAVA_HOME=${JAVA_HOME:-<system default>}"
if [ "$#" -gt 0 ]; then
  exec ./mvnw "$@"
fi
exec ./mvnw spring-boot:run
