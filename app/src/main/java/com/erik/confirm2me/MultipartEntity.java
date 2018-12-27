package com.erik.confirm2me;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simplified multipart entity mainly used for sending one or more files.
 */
public class MultipartEntity {

    private static final String LOG_TAG = "SimpleMultipartEntity";

    private static final String STR_CR_LF = "\r\n";
    private static final byte[] CR_LF = STR_CR_LF.getBytes();
    private static final byte[] TRANSFER_ENCODING_BINARY =
            ("Content-Transfer-Encoding: binary" + STR_CR_LF).getBytes();

    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final String boundary;
    private final byte[] boundaryLine;
    private final byte[] boundaryEnd;
    private boolean isRepeatable;

    private final List<FilePart> fileParts = new ArrayList<FilePart>();

    // The buffer we use for building the message excluding files and the last
    // boundary
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private int bytesWritten;

    private int totalSize;

    public MultipartEntity() {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        boundary = buf.toString();
        boundaryLine = ("--" + boundary + STR_CR_LF).getBytes();
        boundaryEnd = ("--" + boundary + "--" + STR_CR_LF).getBytes();

    }

    public void addPart(String key, String value, String contentType) {
        try {
            out.write(boundaryLine);
            out.write(createContentDisposition(key));
            out.write(createContentType(contentType));
            out.write(CR_LF);
            out.write(value.getBytes());
            out.write(CR_LF);
        } catch (final IOException e) {
            // Shall not happen on ByteArrayOutputStream
            Log.e(LOG_TAG, "addPart ByteArrayOutputStream exception", e);
        }
    }

    public void addPartWithCharset(String key, String value, String charset) {
        if (charset == null) charset = HTTP.UTF_8;
        addPart(key, value, "text/plain; charset=" + charset);
    }

    public void addPart(String key, String value) {
        addPartWithCharset(key, value, null);
    }

    public void addPart(String key, FileBody file) {
        addPart(key, file);
    }

    public void addPart(String key, File file, String type) {
        fileParts.add(new FilePart(key, file, normalizeContentType(type)));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type)
            throws IOException {

        out.write(boundaryLine);

        // Headers
        out.write(createContentDisposition(key, streamName));
        out.write(createContentType(type));
        out.write(TRANSFER_ENCODING_BINARY);
        out.write(CR_LF);

        // Stream (file)
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = inputStream.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }

        out.write(CR_LF);
        out.flush();

        out.close();
    }

    private String normalizeContentType(String type) {
        return type == null ? "application/octet-stream" : type;
    }

    private byte[] createContentType(String type) {
        String result = "Content-Type: " + normalizeContentType(type) + STR_CR_LF;
        return result.getBytes();
    }

    private byte[] createContentDisposition(String key) {
        return (
                "Content-Disposition: form-data; name=\"" + key + "\"" + STR_CR_LF).getBytes();
    }

    private byte[] createContentDisposition(String key, String fileName) {
        return (
                "Content-Disposition: form-data; name=\"" + key + "\"" +
                        "; filename=\"" + fileName + "\"" + STR_CR_LF).getBytes();
    }

    private void updateProgress(int count) {
        bytesWritten += count;
    }

    public void addPart(String video_name, StringBody stringBody) {
        addPart(video_name, stringBody);
    }

    private class FilePart {
        public File file;
        public byte[] header;

        public FilePart(String key, File file, String type) {
            header = createHeader(key, file.getName(), type);
            this.file = file;
        }

        private byte[] createHeader(String key, String filename, String type) {
            ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            try {
                headerStream.write(boundaryLine);

                // Headers
                headerStream.write(createContentDisposition(key, filename));
                headerStream.write(createContentType(type));
                headerStream.write(TRANSFER_ENCODING_BINARY);
                headerStream.write(CR_LF);
            } catch (IOException e) {
                // Can't happen on ByteArrayOutputStream
                Log.e(LOG_TAG, "createHeader ByteArrayOutputStream exception", e);
            }
            return headerStream.toByteArray();
        }

        public long getTotalLength() {
            long streamLength = file.length() + CR_LF.length;
            return header.length + streamLength;
        }

        public void writeTo(OutputStream out) throws IOException {
            out.write(header);
            updateProgress(header.length);

            FileInputStream inputStream = new FileInputStream(file);
            final byte[] tmp = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(tmp)) != -1) {
                out.write(tmp, 0, bytesRead);
                updateProgress(bytesRead);
            }
            out.write(CR_LF);
            updateProgress(CR_LF.length);
            out.flush();
            inputStream.close();
        }
    }

    // The following methods are from the HttpEntity interface

    public long getContentLength() {
        long contentLen = out.size();
        for (FilePart filePart : fileParts) {
            long len = filePart.getTotalLength();
            if (len < 0) {
                return -1; // Should normally not happen
            }
            contentLen += len;
        }
        contentLen += boundaryEnd.length;
        return contentLen;
    }

//    public String getContentType() {
//        return "multipart/form-data; boundary=" + boundary;
//    }


    public void writeTo(OutputStream outstream) throws IOException {
        bytesWritten = 0;
        totalSize = (int) getContentLength();
        out.writeTo(outstream);
        updateProgress(out.size());

        for (FilePart filePart : fileParts) {
            filePart.writeTo(outstream);
        }
        outstream.write(boundaryEnd);
        updateProgress(boundaryEnd.length);
    }

}
