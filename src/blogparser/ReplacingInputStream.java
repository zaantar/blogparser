package blogparser;

import java.io.*;
import java.util.*;


class ReplacingInputStream extends FilterInputStream {

    LinkedList<Integer> inQueue = new LinkedList<Integer>();
    LinkedList<Integer> outQueue = new LinkedList<Integer>();
    final byte[] search, replacement;

    protected ReplacingInputStream(InputStream in, byte[] search,
                                                   byte[] replacement) {
        super(in);
        this.search = search;
        this.replacement = replacement;
    }

    private boolean isMatchFound() {
        Iterator<Integer> inIter = inQueue.iterator();
        for (int i = 0; i < search.length; i++)
            if (!inIter.hasNext() || search[i] != inIter.next())
                return false;
        return true;
    }

    private void readAhead() throws IOException {
        // Work up some look-ahead.
        while (inQueue.size() < search.length) {
            int next = super.read();
            inQueue.offer(next);
            if (next == -1)
                break;
        }
    }


    @Override
    public int read() throws IOException {

        // Next byte already determined.
        if (outQueue.isEmpty()) {

            readAhead();

            if (isMatchFound()) {
            	//System.out.println("MATCH FOUND");
                for (int i = 0; i < search.length; i++)
                    inQueue.remove();

                for (byte b : replacement) 
                    outQueue.offer((int) b);
            } else
                outQueue.add(inQueue.remove());
        }

        return outQueue.remove();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
    	throw new UnsupportedOperationException();
    }
    
    // TODO there is error somewhere
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
    	int readLen = 0;
    	while(readLen < len) {
    		int val = read();
    		if(val == -1) {
    			//System.out.println("READ " + readLen + " BYTES");
    			return readLen;
    		}
    		b[off+readLen] = (byte) val;
    		readLen++;
    	}
		return readLen;
    }
    
}
