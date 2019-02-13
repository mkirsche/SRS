#
# Code for plotting simulated data:
#    Plots for each read (proportion of minimizers shared, length)
#    Colors to correspond to whether or not the read is contained
#
import sys
import matplotlib.pyplot as plt
from matplotlib import colors as mcolors

fn = sys.argv[1]

uncOnly = False

cts = []
lens = []
colors = []

color_options = ['mediumorchid', 'lightskyblue']

with open(fn) as f:
    for line in f:
        tokens = line.split()
        name = tokens[0]
        ct = float(tokens[1])
        category = int(tokens[2])
        
        if uncOnly and category != 1:
            continue
        
        cts.append(ct)
        
        splitname = name.split('_')
        length = int(splitname[2]) - int(splitname[1])
        lens.append(length)
        
        colors.append(color_options[category])
        
plt.scatter(cts, lens, c=colors, s = 1)
plt.title('Simulated results')
plt.xlabel('Max kmers shared')
plt.ylabel('Read length')
curfn = 'sim.png'
if uncOnly:
    curfn = 'unc_sim.png'
plt.savefig(curfn)
plt.close()
plt.cla()
plt.clf()

plt.hist(lens, bins = [1000*i for i in range(0, 100)])
plt.title('Simulated read lengths')
curfn = 'simlengths.png'
plt.savefig(curfn)
if uncOnly:
    curfn = 'unc_simlengths.png'
plt.close()
plt.cla()
plt.clf()

plt.hist(cts, bins = [0.01*i for i in range(0, 100)])
plt.title('Simulated maxshared')
curfn = 'simshared.png'
if uncOnly:
    curfn = 'unc_simshared.png'
plt.savefig(curfn)
plt.close()
plt.cla()
plt.clf()

