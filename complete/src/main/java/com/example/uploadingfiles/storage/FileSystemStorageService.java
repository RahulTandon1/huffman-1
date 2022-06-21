package com.example.uploadingfiles.storage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
//import com.marvinjason.huffmancoding.HuffmanCoding;
//import com.example.uploadingfiles.;
import java.lang.*;
import com.example.uploadingfiles.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(
					Paths.get(file.getOriginalFilename()))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				ByteArrayInputStream stream = new   ByteArrayInputStream(file.getBytes());
				String fileContent = IOUtils.toString(stream, "UTF-8");

				System.out.println("Trying the given Huffman implementation");
				System.out.println(fileContent);
				HuffmanCoding huffman = new HuffmanCoding(fileContent);
				huffman.compress();

				System.out.println("Size before compression: " + huffman.getUncompressedSize());
				System.out.println("Size after compression: " + huffman.getCompressedSize());

//				System.out.println("Compressed string: " + huffman.getCompressedString());
//				FileUtils.writeStringToFile(destinationFile.toFile(), huffman.getCompressedString(),
//						Charset.forName("UTF-8"), false);

				Files.write(destinationFile, huffman.getCompressedCharString().getBytes());
//				System.out.println("Successfully wrote out the file");s
//				System.out.println(huffman.getCompressedCharString());
//				System.out.println("Length remained by 8 is: " +);
//				int length = huffman.getCompressedString().length();
//				int padding =  length % 8;
//				String compressedContentInBinary = huffman.getCompressedString();
//				String paddedContent = String.format("%" + padding + "s", compressedContentInBinary);
//				length += padding + 1;
//				// length 16; i = 0, 8,
//
//				for(int i = 0; i < length; i += 8) {
//					String char = (char)Integer.parseInt(string, 2)
//
//				}

//				Files.copy(inputStream, destinationFile,
//					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}

/*
*
*
* ./mvnw install:install-file \
   -Dfile=/Users/rahultandon/dev/HuffmanCoding.jar  \
   -DgroupId=com.marvinjason.huffmancoding \
   -DartifactId=HuffmanCoding \
   -Dversion=1.0 \
   -Dpackaging=jar \
   -DgeneratePom=true
* */