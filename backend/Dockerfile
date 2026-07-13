FROM python:3.12-slim

# Prevent Python from writing .pyc files and buffering stdout/stderr.
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

# Install dependencies first to leverage Docker layer caching.
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application source.
COPY app.py .

# Run as a non-root user for a smaller attack surface.
RUN useradd --create-home --uid 10001 appuser
USER appuser

EXPOSE 5000

# The FLAG environment variable is injected at runtime by the CTF platform.
# Gunicorn serves the Flask WSGI application.
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "2", "app:app"]
