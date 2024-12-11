# BPMS_OCR_Schedular
This is BPMS OCR Schedular in Spring boot.


#Before running this service locally please make sure you have the required depenedency installed in your Machine.

#OCRMyPdf installation steps (Windows):
1 Download and install python latest version (python-3.11.0a1-amd64.exe)
	To check python version: python –version  [3.9.6]
2 Download and install ghostscript latest version (gs9550w64.exe)
3 Download and install tesseract-ocr latest version (tesseract-ocr-w64-setup-v5.0.0-alpha.20210506.exe)
4 open Command Prompt (Run as administrator)
5 Run following commands one by one 
a)	pip install pypdfocr
b)	pip install Pillow
c)	pip install reportlab
d)	pip install watchdog
e)	pip install pypdf2
f)	pip install matplotlib
g)	pip install tesseract
h)	pip install leptonica
i)	pip install argh
j)	pip install gtp
k)	pip install sgf
l)	pip install pngquant
6 Install ocrmypdf by running command pip install ocrmypdf in command prompt
	To check ocrmypdf version: ocrmypdf –version
7 Copy bpo_ocr_home folder structure in C:\ drive 
8 Open windows PowerShell
	Run following ocrmypdf commanct to test ocr working
ocrmypdf --force-ocr C:\bpo_ocr_home\BPO\Incoming_Files\1.pdf C:\bpo_ocr_home\BPO\Incoming_Files\1001_output.pdf






#OCRMyPdf installation steps (Linux):
1 Download and install python latest version
	To check python version: python –version
2 Download and install ghostscript latest version
3 Download and install tesseract-ocr latest version
4 Open PuTTY
5 Run following commands one by one 
a)	pip3 install pypdfocr
b)	pip3 install Pillow
c)	pip3 install reportlab
d)	pip3 install watchdog
e)	pip3 install pypdf2
f)	pip3 install matplotlib
g)	pip3 install tesseract
h)	pip3 install leptonica
i)	pip3 install argh
j)	pip3 install gtp
k)	pip3 install sgf
l)	pip3 install pngquant

