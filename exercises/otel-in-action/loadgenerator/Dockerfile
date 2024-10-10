FROM alpine:latest
RUN apk --no-cache add curl
COPY --chmod=755 ./generator.sh .
CMD [ "./generator.sh" ]
