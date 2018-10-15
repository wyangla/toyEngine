if __name__ == '__main__':
    from random import randint
    
    # generation posting_test_load
    persistancePath = r'../persistance/posting_test_load'
    pid = -1
    terms = ['a', 'b']
    len = [3, 100]
    with open(persistancePath, 'w') as f:
        lIdx = -1
        for t in terms:
            lIdx += 1
            for i in range(len[lIdx]):
                pid += 1
                f.write("%s %s %s %s %s"%(t, pid, 00, 00, "{}\n"))
                
    # lexicon_test_load
    persistancePath_l = r'../persistance/lexicon_test_load'
    terms = ['a', 'b']
    r = [(0,3), (3,103)]
    with open(persistancePath_l, 'w') as f:
        rIdx = -1
        for t in terms:
            rIdx += 1
            f.write(t + " " + " ".join( map(str, range(r[rIdx][0], r[rIdx][1])) ) + "\n")