package com.walker.devolay;

/**
 * An internal interface to track where frames have been allocated.
 * This is implemented by {@link DevolaySender}, {@link DevolayReceiver}, and {@link DevolayFrameSync}, as they need to
 * free their own frame buffers.
 *
 * When a frame is returned by a receiver, sender, or framesync, it will eventually need to be freed by that same
 * receiver, so each frame stores a {@link DevolayFrameCleaner} which it will call to destroy its buffer when needed.
 */
abstract class DevolayFrameCleaner {
    abstract void freeVideo(DevolayVideoFrame videoFrame);
    abstract void freeAudio(DevolayAudioFrame audioFrame);
    abstract void freeMetadata(DevolayMetadataFrame metadataFrame);
}
