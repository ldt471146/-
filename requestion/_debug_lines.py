import importlib.util, pathlib 
p=pathlib.Path('requestion/generate_softcopy_docs.py') 
spec=importlib.util.spec_from_file_location('g', p) 
m=importlib.util.module_from_spec(spec) 
spec.loader.exec_module(m) 
lines=m.collect_source_lines() 
front=lines[:1500] 
print('total',len(front),'empty',sum(1 for x in front if not x.strip())) 
for i in range(0,250,50): 
    chunk=front[i:i+50] 
    nonempty=sum(1 for x in chunk if x.strip()) 
    print('page',i//50+1,'nonempty',nonempty,'sample',chunk[:3]) 
