package com.mind.freeocr;

import java.io.File;

public class FileInfo implements Comparable<FileInfo> {

	File file;

	String fileName;

	long fileSize;


	public FileInfo(final File file, final String fileName, final long fileSize) {
		this.file = file;
		this.fileSize = fileSize;
		this.fileName = fileName;
	}

	/**
	 * @return the fileName
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FileInfo other = (FileInfo) obj;
		if (fileName == null) {
			if (other.fileName != null) return false;
		}
		else if (!fileName.equals(other.fileName)) return false;
		if (fileSize != other.fileSize) return false;
		return true;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(File file) {
		this.file = file;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}


	@Override
	public int compareTo(FileInfo other) {
		return Long.compare(fileSize, other.fileSize);
	}



}
