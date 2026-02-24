package fi.celssi.chatdm.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Component
@Profile({"cloud", "gcs"})
public class GcsJournalStorage implements JournalStorage {

    private final Storage storage;
    private final String bucket;
    private final String basePrefix;

    public GcsJournalStorage(Storage storage,
                            @Value("${chatdm.journal.base-path:}") String journalBasePath) {
        this.storage = storage;
        // Parse gs://bucket/prefix format
        if (journalBasePath != null && journalBasePath.startsWith("gs://")) {
            String withoutGs = journalBasePath.substring(5);
            int slash = withoutGs.indexOf('/');
            this.bucket = slash > 0 ? withoutGs.substring(0, slash) : withoutGs;
            this.basePrefix = slash > 0 ? withoutGs.substring(slash + 1).replaceAll("/$", "") + "/" : "journal/";
        } else {
            this.bucket = "";
            this.basePrefix = "journal/";
        }
    }

    @PostConstruct
    public void init() {
        if (bucket.isEmpty()) {
            throw new IllegalStateException("chatdm.journal.base-path must be set for cloud profile (e.g. gs://bucket/journal)");
        }
    }

    private String objectName(String subDir, String fileName) {
        return basePrefix + subDir + "/" + fileName;
    }

    @Override
    public String read(String subDir, String fileName) throws IOException {
        Blob blob = storage.get(BlobId.of(bucket, objectName(subDir, fileName)));
        if (blob == null) {
            return null;
        }
        byte[] content = blob.getContent();
        return content != null ? new String(content, StandardCharsets.UTF_8) : null;
    }

    @Override
    public void write(String subDir, String fileName, String content) throws IOException {
        storage.create(BlobInfo.newBuilder(BlobId.of(bucket, objectName(subDir, fileName))).build(),
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean exists(String subDir, String fileName) throws IOException {
        Blob blob = storage.get(BlobId.of(bucket, objectName(subDir, fileName)));
        return blob != null && blob.exists();
    }

    @Override
    public List<String> list(String subDir) throws IOException {
        String prefix = basePrefix + subDir + "/";
        Iterable<Blob> blobs = storage.list(bucket, Storage.BlobListOption.prefix(prefix)).iterateAll();
        List<String> result = new ArrayList<>();
        for (Blob b : blobs) {
            String name = b.getName();
            String relative = name.substring(prefix.length());
            if (relative.contains("/")) continue; // skip objects in subfolders
            if (!relative.isEmpty()) result.add(relative);
        }
        return result;
    }

    @Override
    public List<String> listSubdirs(String subDir) throws IOException {
        String prefix = basePrefix + subDir + "/";
        Iterable<Blob> blobs = storage.list(bucket, Storage.BlobListOption.prefix(prefix)).iterateAll();
        Set<String> subdirs = new HashSet<>();
        for (Blob b : blobs) {
            String name = b.getName();
            String relative = name.substring(prefix.length());
            if (relative.contains("/")) {
                String firstSegment = relative.substring(0, relative.indexOf('/'));
                subdirs.add(firstSegment);
            }
        }
        return subdirs.stream().sorted().toList();
    }

    @Override
    public void append(String subDir, String fileName, String content) throws IOException {
        String existing = read(subDir, fileName);
        String newContent = (existing != null ? existing : "") + content;
        write(subDir, fileName, newContent);
    }

    @Override
    public void delete(String subDir, String fileName) throws IOException {
        BlobId blobId = BlobId.of(bucket, objectName(subDir, fileName));
        try {
            storage.delete(blobId);
        } catch (com.google.cloud.storage.StorageException e) {
            if (e.getCode() != 404) {
                throw new IOException("Failed to delete " + fileName, e);
            }
        }
    }
}
