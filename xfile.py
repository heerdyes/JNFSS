import os
import sys

# total loc variable
LOC = 0
# indices 0, 1 and 2 hold scriptnm dirnm and langnm
cmd = sys.argv

# function to determine if target is a source file
def srcfile(file, lang):
    (f, x) = os.path.splitext(file)
    return x == lang

# function to count the LOC
def countloc(fnm):
    loc = 0
    with open(fnm) as f:
        for ln in f:
            loc += 1
    return loc

# function to walk the dir tree
def walkdir(f, x):
    q = [f]
    z = []
    while len(q) > 0:
        t = q[0]
        q = q[1:]
        # if dir push to queue after joining
        if os.path.isdir(t):
            for j in os.listdir(t):
                q = q + [os.path.join(t, j)]
        else:
            # if its a source file add it to z
            if srcfile(t, x):
                print(os.path.basename(t))
                z = z + [t]
    return z

# now the script
td = cmd[1]
tx = cmd[2]
print('loaded td and tx...')
seq = walkdir(td, tx)
# run through the list
for j in seq:
    LOC += countloc(j)
# print loc
print('total lines of code = ', LOC)
print('*** DONE ***')
