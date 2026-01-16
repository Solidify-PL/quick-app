FROM ubuntu:24.04
WORKDIR /app

# Install Podman and dependencies
RUN apt-get update && apt-get install -y \
    podman \
    runc \
    crun \
    fuse-overlayfs \
    bash \
    && rm -rf /var/lib/apt/lists/*

# (Optional) Install zlib1g for non-static binaries
RUN apt-get update && apt-get install -y zlib1g

# Configure Podman storage
RUN mkdir -p /var/lib/containers /root/.config/containers && \
    echo "[storage]" > /root/.config/containers/storage.conf && \
    echo "driver = \"fuse-overlayfs\"" >> /root/.config/containers/storage.conf && \
    echo "root = \"/var/lib/containers\"" >> /root/.config/containers/storage.conf

# Configure registries.conf for unqualified image names
RUN echo "[registries.search]" > /etc/containers/registries.conf && \
    echo "registries = ['docker.io']" >> /etc/containers/registries.conf

# Set up the 'docker' alias for 'podman'
RUN echo "alias docker='podman'" >> /root/.bashrc

COPY qa /app/qa
WORKDIR /app

# Set Bash as the default shell (so that the alias works)
SHELL ["/bin/bash", "-c"]
CMD ["/bin/bash"]
