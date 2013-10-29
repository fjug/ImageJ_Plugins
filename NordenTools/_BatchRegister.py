from ij import IJ
from ij import WindowManager
import glob
import os
import sys
from java.lang.System import getProperty
sys.path.append(getProperty("fiji.dir") + "/plugins/NordenTools")
import NordenTools as nt

from setup import *

def doPairRegistration(tpA,tpB):
  fileA = "raw_"+tpA+".tif"
  fileB = "raw_"+tpB+".tif"
  
  IJ.open(output_dir+fileA)
  IJ.open(output_dir+fileB)

  IJ.run("Rigid Registration", "initialtransform=[] n=1 tolerance=1.000 level=5 stoplevel=2 materialcenterandbbox=[] " + 
         "template=%s measure=Euclidean"%(fileA))
  IJ.selectWindow("Matrix")
  IJ.saveAs("Text", "/Users/jug/MPI/ProjectNorden/output/matrix_%s_%s.txt"%(tpA,tpB))
  window = WindowManager.getWindow("Matrix")
  window.close()
  IJ.run("Close All")


filenum = len(time_points)
if filenum>1:
  num = 1
  #copy center stack (registered by definition)
  tpCenter = time_points[(filenum-1)/2+1]
  IJ.log( "Init Registration: copy center stack (%s)."%(tpCenter) )
  imp = IJ.openImage(output_dir+"raw_"+tpCenter+".tif")
  IJ.save(imp,output_dir+"reg_%s.tif"%tpCenter)
  imp.close()
  
  # 1) we go backwards from center
  for i in range((filenum-1)/2,-1,-1):
    IJ.log( "Registering (%d/%d)...\n <<< %s <<< %s"%(num,filenum-1,time_points[i],time_points[i+1]) )
    doPairRegistration(time_points[i+1],time_points[i]) # mind reverse order!
    num+=1
  # 2) we go forward from center
  for i in range((filenum-1)/2+1,filenum-1):
    IJ.log( "Registering (%d/%d)...\n >>> %s >>> %s"%(num,filenum-1,time_points[i],time_points[i+1]) )
    doPairRegistration(time_points[i],time_points[i+1])
    num+=1
else:
    IJ.log( "Registration of ONE file done... Easy! ;)\nYou might consider registering more then one file...")