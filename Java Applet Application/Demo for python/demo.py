import snap
import sys
UGraph = snap.TUNGraph.New()
CmtyV = snap.TCnComV()
ins = open(sys.argv[1], "r")  #input file path
last=-1
for line in ins:
    words = line.split()
    #if words[0]!=last:
    #    UGraph.AddNode(int(words[0]))
    if words[0]>last:
        last=int(words[0])
    if words[1]>last:
        last=int(words[1])
ins.close()
print last
for i in range(0,last+1):
    UGraph.AddNode(i)
print sys.argv[1]
ins = open(sys.argv[1], "r")
for line in ins:
    words = line.split()
    UGraph.AddEdge(int(words[0]), int(words[1]))
ins.close()
print "over"
modularity = snap.CommunityCNM(UGraph, CmtyV)    #snap.Infomap  snap.CommunityCNM  snap.CommunityGirvanNewman
for Cmty in CmtyV:
    print "Community: "
    for NI in Cmty:
        print NI
print "The modularity of the network is %f" % modularity