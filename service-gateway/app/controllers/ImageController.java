package controllers;

import akka.actor.ActorSystem;
import io.swagger.annotations.*;
import org.encentral.ImageInverterImpl;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Helper;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Api(tags = "Image Inverter")
public class ImageController extends Controller {

    private final ActorSystem actorSystem;
    private final ImageInverterImpl imageInverter;

    @Inject
    public ImageController(ActorSystem actorSystem, ImageInverterImpl imageInverter) {
        this.actorSystem = actorSystem;
        this.imageInverter = imageInverter;
    }

    @ApiOperation(value = "Upload Image", consumes = "multipart/form-data")
    @ApiResponses(
            value = {
                    @ApiResponse( code=201, message = "Image uploaded and inverted successfully.", response = String.class),
                    @ApiResponse(code = 400, message = "No image file provided.", response = String.class),
                    @ApiResponse(code = 404, message = "Invalid image file format. Only PNG, JPG, and JPEG formats are supported.", response = String.class),
                    @ApiResponse(code = 500, message = "Failed to upload and invert the image.", response = String.class)
            }
    )
    @ApiImplicitParam(
            name = "image",
            value = "Image-file",
            dataType = "file",
            paramType = "form",
            required = true
    )
    public Result uploadImage(Http.Request request) {
        Http.MultipartFormData<File> formData = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> filePart = formData.getFile("image");
        if (filePart != null) {
            File file = filePart.getFile();
            String originalFilename = filePart.getFilename();

            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")){
                String filename = UUID.randomUUID().toString() + extension;
                String destinationPath = "public/images/" + filename;
                String invertedPath = "public/images/inverted_" + filename;

                try {
                    Files.move(file.toPath(), Paths.get(destinationPath));
                    imageInverter.invertColors(destinationPath, invertedPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    return internalServerError(Helper.createResponse("Failed to upload and invert the image.", false));
                }

                return ok(Helper.createResponse("Image uploaded and inverted successfully.", true));
            } else {
                return badRequest(Helper.createResponse("Invalid image file format. Only PNG, JPG, and JPEG formats are supported.", false));
            }

        } else {
            return badRequest(Helper.createResponse("No image file provided.", false));
        }
    }

    @ApiOperation(value = "Get Inverted Image")
    @ApiResponses(
            value = {
                    @ApiResponse( code=201, message = "File retrieved successfully", response = String.class),
                    @ApiResponse(code = 404, message = "Can't find image", response = String.class)
            }
    )
    public Result getImage(String filename) {
        File file = new File("public/images/inverted_" + filename);
        if (file.exists()) {
            return ok(file);
        } else {
            return notFound(Helper.createResponse("Can't find image", false));
        }
    }
}
