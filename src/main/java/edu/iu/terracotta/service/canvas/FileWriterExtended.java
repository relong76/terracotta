package edu.iu.terracotta.service.canvas;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import edu.ksu.canvas.exception.InvalidOauthTokenException;
import edu.ksu.canvas.interfaces.CanvasWriter;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.File;
import edu.ksu.canvas.requestOptions.UploadOptions;

public interface FileWriterExtended extends CanvasWriter<File, FileWriterExtended> {

    Optional<Deposit> initializeUpload(UploadOptions uploadOptions) throws InvalidOauthTokenException, IOException;
    Optional<File> upload(Deposit deposit, InputStream in, String filename) throws IOException;
    Optional<File> delete(long id) throws IOException;

}
