"""
Java-Key-Vault: FastAPI microservice implementing Java Key Vault domain logic
"""
import time
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Java-Key-Vault", version="3.0.0")

class EventRequest(BaseModel):
    event_type: str
    payload: dict
    source: str

events_store = []

@app.post("/api/v1/events")
def publish_event(event: EventRequest):
    events_store.append(event.dict())
    return {"status": "published", "event_type": event.event_type, "total_events": len(events_store)}

@app.get("/api/v1/events")
def list_events():
    return {"events": events_store[-10:], "total": len(events_store)}


@app.get("/health")
def health():
    return {"status": "healthy", "service": "Java-Key-Vault", "timestamp": int(time.time())}
