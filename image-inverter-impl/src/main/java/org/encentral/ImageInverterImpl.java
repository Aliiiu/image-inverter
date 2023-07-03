package org.encentral;

import akka.actor.ActorSystem;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

public class ImageInverterImpl implements IImageInverter{

    private final ActorSystem system;
    private final Materializer materializer;

    @Inject
    public ImageInverterImpl(ActorSystem system) {
        this.system = system;
        this.materializer = Materializer.createMaterializer(system);
    }

    @Inject
    public void invertColors(String sourcePath, String destinationPath){
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);

        Source<ByteString, CompletionStage<IOResult>> sourceStream = FileIO.fromPath(source);
        Sink<ByteString, CompletionStage<IOResult>> destinationStream = FileIO.toPath(destination);

        sourceStream
                .map(ByteString::toArray)
                .map(this::invertColor)
                .map(ByteString::fromArray)
                .runWith(destinationStream, materializer);
    }

    private byte[] invertColor(byte[] pixel) {
        byte red = pixel[0];
        byte green = pixel[1];
        byte blue = pixel[2];

        pixel[0] = green;
        pixel[1] = blue;
        pixel[2] = red;

        return pixel;
    }
}
