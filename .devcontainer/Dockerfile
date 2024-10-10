FROM maven:3-eclipse-temurin-21  AS dind

ARG DEBIAN_FRONTEND=noninteractive
ENV PIP_ROOT_USER_ACTION=ignore

RUN set -eux && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    sudo \
    ca-certificates \
    iptables \
    net-tools \
    openssl \
    pigz \
    xz-utils \
    procps \
    supervisor

COPY --from=docker:27-dind /usr/local/bin /usr/local/bin
COPY --from=docker:27-dind /usr/local/libexec/docker/cli-plugins /usr/local/libexec/docker/cli-plugins

COPY entrypoint.sh /usr/local/bin
RUN chmod +x /usr/local/bin/entrypoint.sh

RUN update-alternatives --set iptables /usr/sbin/iptables-legacy
RUN update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy

VOLUME /var/lib/docker

RUN apt-get update && \
    apt-get install --yes --no-install-recommends \
    sudo \
    bash \
    curl \
    build-essential \
    python3.12 \
    python-is-python3 \
    python3-dev \
    python3-pip \
    python3-setuptools \
    python3-wheel \
    python3-psutil && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*

RUN rm -rf /usr/lib/python3*/EXTERNALLY-MANAGED

COPY requirements.txt /usr/local/src/requirements.txt

RUN pip install -r /usr/local/src/requirements.txt

WORKDIR /workspace

EXPOSE 5000

ENTRYPOINT [ "entrypoint.sh" ]