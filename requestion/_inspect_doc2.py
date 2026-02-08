import zipfile,glob,pathlib,os,re 
files=[x for x in glob.glob('requestion/*.docx') if not pathlib.Path(x).name.startswith('~$')] 
p=sorted(files,key=lambda x: os.path.getsize(x))[0] 
x=zipfile.ZipFile(p).read('word/document.xml').decode('utf-8','ignore') 
parts=re.split('w:type=\"page\"',x) 
counts=[s.count('<w:t') for s in parts] 
print('file',p) 
print('min',min(counts),'max',max(counts),'pages',len(counts)) 
print('counts',counts) 
