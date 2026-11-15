"""Offline vocal separation; optional local lyric transcription. No hosted AI API."""
import argparse,json,os,sys
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('source');p.add_argument('output');p.add_argument('--models',required=True);p.add_argument('--transcribe',action='store_true');a=p.parse_args()
out=Path(a.output);out.mkdir(parents=True,exist_ok=True)
from audio_separator.separator import Separator
separator=Separator(output_dir=str(out),output_format='WAV',model_file_dir=a.models)
separator.load_model(model_filename='UVR-MDX-NET-Inst_HQ_3.onnx')
files=separator.separate(a.source)
instrumental=next((str(out/f) for f in files if 'instrumental' in f.lower()),None)
vocals=next((str(out/f) for f in files if 'vocal' in f.lower()),None)
if not instrumental:raise RuntimeError('The model did not produce an instrumental stem')
result={'instrumental':instrumental,'vocals':vocals}
if a.transcribe and vocals:
 try:
  from faster_whisper import WhisperModel
  model=WhisperModel('base',device='cpu',compute_type='int8',download_root=str(Path(a.models)/'whisper'))
  segments,_=model.transcribe(vocals,vad_filter=True,beam_size=3)
  result['lyrics']=[{'time':float(s.start),'text':s.text.strip()} for s in segments if s.text.strip()]
  result['lyricsSource']='Local transcription — review for mistakes'
 except Exception as e:result['lyricsError']='Local transcription unavailable: '+type(e).__name__
(out/'result.json').write_text(json.dumps(result),encoding='utf-8')
print('KARAOKE_RESULT_READY',flush=True)
