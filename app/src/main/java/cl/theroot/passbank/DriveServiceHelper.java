package cl.theroot.passbank;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("appDataFolder").setFields("nextPageToken, files(id, name, createdTime, size)").execute());
    }

    public Task<File> uploadFile(final java.io.File localFile, final String mimeType) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setMimeType(mimeType)
                    .setName(localFile.getName());
            FileContent fileContent = new FileContent(mimeType, localFile);

            return mDriveService.files().create(metadata, fileContent).execute();
        });
    }

    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);

            return null;
        });
    }

    public Task<Void> deleteFiles(final List<String> listaIds) {
        return Tasks.call(mExecutor, () -> {
            for (String id : listaIds) {
                mDriveService.files().delete(id).execute();
            }

            return null;
        });
    }
}
