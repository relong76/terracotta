package edu.iu.terracotta.service.canvas.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

import edu.iu.terracotta.service.canvas.FileWriterExtended;
import edu.ksu.canvas.exception.InvalidOauthTokenException;
import edu.ksu.canvas.impl.BaseImpl;
import edu.ksu.canvas.impl.FileImpl;
import edu.ksu.canvas.interfaces.FileReader;
import edu.ksu.canvas.model.Deposit;
import edu.ksu.canvas.model.File;
import edu.ksu.canvas.net.Response;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.UploadOptions;

public class FileExtendedImpl extends BaseImpl<File, FileReader, FileWriterExtended> implements FileReader, FileWriterExtended {

    private FileImpl fileImpl;

    public FileExtendedImpl(String canvasBaseUrl, Integer apiVersion, OauthToken oauthToken, RestClient restClient, int connectTimeout, int readTimeout, Integer paginationPageSize, Boolean serializeNulls) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient, connectTimeout, readTimeout, paginationPageSize, serializeNulls);
        fileImpl = new FileImpl(canvasBaseUrl, apiVersion, oauthToken, restClient, connectTimeout, readTimeout, paginationPageSize, serializeNulls);
    }

    @Override
    public Optional<File> getFile(String url) throws IOException {
        return fileImpl.getFile(url);
    }

    @Override
    public Optional<Deposit> initializeUpload(UploadOptions uploadOptions) throws InvalidOauthTokenException, IOException {
        Response response = canvasMessenger.sendToCanvas(
            oauthToken,
            buildCanvasUrl("users/self/files", Collections.emptyMap()),
            uploadOptions.getOptionsMap()
        );

        return responseParser.parseToObject(Deposit.class, response);
    }

    @Override
    public Optional<File> upload(Deposit deposit, InputStream in, String filename) throws IOException {
        return fileImpl.upload(deposit, in, filename);
    }

    @Override
    public Optional<File> delete(long id) throws IOException {
        Response response = canvasMessenger.deleteFromCanvas(
            oauthToken,
            buildCanvasUrl(String.format("files/%s", id) , Collections.emptyMap()),
            Collections.emptyMap()
        );

        return responseParser.parseToObject(File.class, response);
    }

    @Override
    protected Type listType() {
        return new TypeToken<List<File>>() {}.getType();
    }

    @Override
    protected Class<File> objectType() {
        return File.class;
    }

}
