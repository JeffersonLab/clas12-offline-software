/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;

/**
 *
 * @author gavalian
 */
public class BosDataEvent implements DataEvent {

	private ByteBuffer bosBCS = null;
	private BosDataDictionary bankDictionary = null;
	private int RunNumber;
	private int EventNumber;
	private DataEventType eventType = DataEventType.UNDEFINED;

	public BosDataEvent() {

	}

	public BosDataEvent(ByteBuffer buffer) {
		bosBCS = buffer;
	}

	public BosDataEvent(ByteBuffer buffer, BosDataDictionary dict) {
		bosBCS = buffer;
		bankDictionary = dict;
		this.updateRunEventNumber();
	}

	public String[] getBankList() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void findStructure() {
		String name = "RUNEVENT";
		byte[] bufferBytes = bosBCS.array();
		byte[] nameBytes = name.getBytes();
		for (int loop = 0; loop < bufferBytes.length; loop++) {
			if (bufferBytes[loop] == nameBytes[0]) {
				String header = new String(bufferBytes, loop, 8);
				// System.out.println("[StrcutureFinder] ----> Found R at pos = "
				// + loop + " header = " + header);
				if (header.compareTo(name) == 0) {
					System.out.println("[StrcutureFinder] ----> Found events at pos = " + loop);
				}
			}
		}
	}

	public void showBankInfo(String bank) {
		int index = this.getBankIndex(bank, 0);
		if (index >= 0) {
			BosBankHeader header = new BosBankHeader(index);
			int ncols = this.unsignedIntFromBuffer(bosBCS, header.NCOLS_WORD_INDEX);
			int nrows = this.unsignedIntFromBuffer(bosBCS, header.NROWS_WORD_INDEX);
			int nword = this.unsignedIntFromBuffer(bosBCS, header.NWORDS_WORD_INDEX);
			int ndata = this.unsignedIntFromBuffer(bosBCS, header.NDATA_START_INDEX);
			System.out.println(String.format("BANK [%6s] i=%8d c=%8d r=%8d w=%8d d=%8d l=%8d", bank, index, ncols, nrows, nword, ndata,
			        bosBCS.array().length));
		} else {
			System.out.println("[showBankInfo]----> marker for bank " + bank + " not found ");
		}

	}

	public ArrayList<BosBankStructure> getBankStructures(String name, int number) {
		ArrayList<Integer> index = this.getBankMultiIndex(name, number);
		ArrayList<BosBankStructure> structures = new ArrayList<BosBankStructure>();
		int banksize = this.bankDictionary.getDescriptor(name).getProperty("banksize");
		for (int loop = 0; loop < index.size(); loop++) {

			BosBankHeader header = new BosBankHeader(index.get(loop));
			int ncols = this.unsignedIntFromBuffer(bosBCS, header.NCOLS_WORD_INDEX);
			int nrows = this.unsignedIntFromBuffer(bosBCS, header.NROWS_WORD_INDEX);
			int nword = this.unsignedIntFromBuffer(bosBCS, header.NWORDS_WORD_INDEX);
			int fpackoffset = (bosBCS.getInt(header.NDATA_START_INDEX) - 1) * 4;

			int offset = BosBankStructure.dataOffset(bosBCS, index.get(loop));
			int realoffset = offset - index.get(loop);

			int wordoffset = realoffset / 4;
			if ((realoffset) % 4 != 0)
				wordoffset += 1;
			int dataposition = wordoffset * 4;
			// System.out.println("getBankStructure::: BANK [" + name + "] " + index.get(loop) + " " + offset +
			// " RO " + realoffset + " WO " + wordoffset);
			BosBankStructure struct = new BosBankStructure();

			struct.NROWS = nrows;
			struct.NCOLS = ncols;
			struct.NWORDS = nword;
			struct.BANKSIZE = banksize;
			struct.DATA = new byte[nword * 4];
			System.arraycopy(bosBCS.array(), index.get(loop) + dataposition, struct.DATA, 0, nword * 4);
			struct.bankName = name;
			struct.BANKNUMBER = number;
			structures.add(struct);
			/*
			 * if(index.size()>1){ System.out.println(loop + " : OFFSET " + realoffset + " / " + wordoffset + " FPACK " + fpackoffset + "  " + struct); }
			 */
			// BosBankStructure stuct = new BosBankStructure();
		}
		return structures;
	}

	final void updateRunEventNumber() {
		if (this.checkBankConsistency("HEAD", 0) == false)
			return;
		/*
		 * BosDataBank bank = (BosDataBank) this.getBank("HEAD"); if(bank!=null){ RunNumber = bank.getInt("NRUN")[0]; EventNumber = bank.getInt("NEVENT")[0]; }
		 */
	}

	int getEventNumber() {
		if (this.checkBankConsistency("HEAD", 0) == false)
			return 0;
		BosDataBank bank = (BosDataBank) this.getBank("HEAD");
		return bank.getInt("NEVENT")[0];
	}

	int unsignedIntFromBuffer(ByteBuffer b, int offset) {
		byte b1 = b.get(offset);
		byte b2 = b.get(offset + 1);
		byte b3 = b.get(offset + 2);
		byte b4 = b.get(offset + 3);
		/*
		 * System.err.println("b = " + Integer.toHexString(b1) + "-" + Integer.toHexString(b2) + "-" + Integer.toHexString(b3) + "-" + Integer.toHexString(b4) + "-"
		 * );
		 */
		int s = 0;
		s = s | (b4 & 0xff);
		s = (s << 8);
		s = s | (b3 & 0xff);
		s = (s << 8);
		s = s | (b2 & 0xff);
		s = (s << 8);
		s = s | (b1 & 0xff);
		return s;
		// int s = 0;
		// s = s | b4;
		// s = (s << 8);
		// s = s | b3;
		// s = (s << 8);
		// s = s | b2;
		// s = (s << 8);
		// s = s | b1;
		// return s;
	}

	public void showBank(String name, int num) {
		int runno = 0;
		int runev = 0;
		if (this.hasBank("HEAD") == true) {
			BosDataBank bank = (BosDataBank) this.getBank("HEAD");
			int[] RN = bank.getInt("NRUN");
			int[] RE = bank.getInt("NEVENT");
			runno = RN[0];
			runev = RE[0];
		}
		int index = this.getBankIndex(name, num);
		int ncols = 0;
		int nrows = 0;
		int nwords = 0;
		if (index >= 0) {
			BosBankHeader header = new BosBankHeader(index);
			ncols = bosBCS.get(header.NCOLS_WORD_INDEX);
			nrows = bosBCS.get(header.NROWS_WORD_INDEX);
			// int nwords = bosBCS.get(header.NWORDS_WORD_INDEX);
			nwords = this.unsignedIntFromBuffer(bosBCS, header.NWORDS_WORD_INDEX);
		}
		int banksize = this.bankDictionary.getDescriptor(name).getProperty("banksize");
		int nbytes = nwords * 4;
		Integer nwords_i = nwords;
		int nwords_correct = nwords & 0x000000FF;

		System.out.println(String.format("[ %6d : %6d ] [ %6s ] %5d %5d %5d %5d %5d [%5d %5d] ", runno, runev, name, index, ncols, nrows,
		        nwords, nwords_correct, nrows * banksize, nbytes));
	}

	boolean checkBankConsistency(String bankname, int banknumber) {
		boolean consistency = true;
		int index = this.getBankIndex(bankname, banknumber);
		if (index < 0)
			return false;
		BosBankHeader header = new BosBankHeader(index);
		int ncols = bosBCS.get(header.NCOLS_WORD_INDEX);
		int nrows = bosBCS.get(header.NROWS_WORD_INDEX);
		// int nwords = bosBCS.get(header.NWORDS_WORD_INDEX);
		int nwords = this.unsignedIntFromBuffer(bosBCS, header.NWORDS_WORD_INDEX);
		// bosBCS.get(header.NWORDS_WORD_INDEX);

		int banksize = this.bankDictionary.getDescriptor(bankname).getProperty("banksize");
		int nbytes = nwords * 4;
		Integer nwords_i = nwords;
		int nwords_correct = nwords & 0x000000FF;
		// int nevent = this.getEventNumber();
		int nevent = 0;

		if (nrows * banksize > nbytes) {
			/*
			 * System.out.println("[check-bank]---> ERROR : consistency check " + "failed run # " + RunNumber + " event # " + EventNumber + " for bank " + bankname +
			 * " ncols = " + nrows + " nrows = " + nrows + " banksize = " + banksize + " bytes = " + nrows*banksize + " entry bytes = " + nbytes + " bin = " +
			 * Integer.toBinaryString(nwords) + " hex = " + Integer.toHexString(nwords) );
			 */
			return false;
		}
		return consistency;
	}

	public void dumpBufferToFile() {
		// System.out.println("DUMPING EVENT # " + this.currentEventInBuffer + " BUFF " + this.currentBufferPosition);
		int nrun = 0;
		int nevt = 0;
		if (this.hasBank("HEAD") == true) {
			BosDataBank bank = (BosDataBank) this.getBank("HEAD");
			nrun = bank.getInt("NRUN")[0];
			nevt = bank.getInt("NEVENT")[0];
		}

		String filename = "bosEventBuffer_" + nrun + "_" + nevt + ".buff";
		System.out.println("[BosDataEvent] ----> Producing EVENT DUMP : " + filename);
		try {
			boolean append = false;
			FileChannel wChannel = new FileOutputStream(new File(filename), append).getChannel();
			wChannel.write(this.bosBCS);
			wChannel.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BosDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void findBank(String name) {
		byte[] bufferBytes = bosBCS.array();
		byte[] nameBytes = name.getBytes();
		for (int loop = 0; loop < bufferBytes.length; loop++) {

			if (bufferBytes[loop] == nameBytes[0]) {
				String bankHeader = new String(bufferBytes, loop, 4);
				if (bankHeader.compareTo(name) == 0) {
					System.out.println("----> found bank " + name + " in position " + loop);
					int startIndex = loop + 4;
					int number = bosBCS.getInt(startIndex + 4);
					int ncols = bosBCS.getInt(startIndex + 8);
					int nrows = bosBCS.getInt(startIndex + 12);
					int nwords = bosBCS.getInt(startIndex + 24);
					System.out.println("----> found bank " + name + " in position " + loop + " rows = " + nrows + " cols = " + ncols
					        + "  number = " + number + " words = " + nwords);
				}
			}
		}
	}

	int getBankIndex(String bank, int nr) {
		int index = -1;
		byte[] bufferBytes = bosBCS.array();
		byte[] nameBytes = bank.getBytes();
		for (int loop = 0; loop < bufferBytes.length; loop++) {
			if (bufferBytes[loop] == nameBytes[0] && loop < bufferBytes.length - 24) {
				String bankHeader = new String(bufferBytes, loop, 4);
				int number = bosBCS.getInt(loop + 8);
				if (number == nr && bankHeader.compareTo(bank) == 0)
					return loop;
			}
		}
		return index;
	}

	int getBankIndex(String bank, int nr, int offset) {
		int index = -1;
		bosBCS.rewind();
		byte[] bufferBytes = bosBCS.array();
		byte[] nameBytes = bank.getBytes();
		for (int loop = offset; loop < bufferBytes.length; loop++) {
			if (bufferBytes[loop] == nameBytes[0] && loop < bufferBytes.length - 24) {
				String bankHeader = new String(bufferBytes, loop, 4);
				int number = bosBCS.getInt(loop + 8);
				if (number == nr && bankHeader.compareTo(bank) == 0)
					return loop;
			}
		}
		return index;
	}

	public String[] getColumnList(String bank_name) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public DataDictionary getDictionary() {
		return bankDictionary;
	}

	public void appendBank(DataBank bank) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public boolean hasBank(String name) {
		// int index = this.getBankIndex(name, 0);
		// if(index>=0) return true;
		String __bankname = name;
		int __banknumber = 0;
		if (name.contains(":") == true) {
			String[] tokens = name.split(":");
			__bankname = tokens[0];
			__banknumber = Integer.parseInt(tokens[1]);
		}
		ByteBuffer bcsBank = this.getBankData(__bankname, __banknumber);
		// ArrayList<Integer> index = this.getBankMultiIndex(__bankname, __banknumber);
		if (bcsBank.capacity() > 0)
			return true;
		return false;
		/*
		 * return this.checkBankConsistency(__bankname,__banknumber);
		 */
	}

	public ByteBuffer getBankData(String bankname, int banknum) {
		ArrayList<BosBankStructure> structures = this.getBankStructures(bankname, banknum);
		if (structures.size() == 1) {
			ByteBuffer buffer = ByteBuffer.wrap(structures.get(0).DATA);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			return buffer;
		}

		if (structures.size() == 2) {
			if (structures.get(0).isComplete() == true) {
				ByteBuffer buffer = ByteBuffer.wrap(structures.get(0).DATA);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				return buffer;
			}

			if (structures.get(1).isComplete() == true) {
				ByteBuffer buffer = ByteBuffer.wrap(structures.get(1).DATA);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				return buffer;
			}

			BosBankStructure struct = BosBankStructure.combine(structures.get(0), structures.get(1));
			if (struct.isComplete() == true) {
				ByteBuffer buffer = ByteBuffer.wrap(struct.DATA);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				/*
				 * if(structures.get(0).isComplete()==true||structures.get(1).isComplete()){ System.out.println("---------> FOUND STRUCTURE TWO PIECES  ");
				 * System.out.println(structures.get(0)); System.out.println(structures.get(1)); this.dumpBufferToFile(); }
				 */
				return buffer;
			}
		}

		return ByteBuffer.wrap(new byte[0]);
	}

	/*
	 * public ByteBuffer getBankData(String bankname, int banknum){
	 * 
	 * ArrayList<Integer> index = this.getBankMultiIndex(bankname, banknum);
	 * 
	 * ArrayList<Integer> dataP = new ArrayList<Integer>(); ArrayList<Integer> dataL = new ArrayList<Integer>();
	 * 
	 * int totalLength = 0;
	 * 
	 * for(int loop = 0; loop < index.size(); loop++){ BosBankHeader header = new BosBankHeader(index.get(loop)); int ncols = bosBCS.get(header.NCOLS_WORD_INDEX);
	 * int nrows = bosBCS.get(header.NROWS_WORD_INDEX); //int nwords = bosBCS.get(header.NWORDS_WORD_INDEX); int nwords = this.unsignedIntFromBuffer(bosBCS,
	 * header.NWORDS_WORD_INDEX); int banksize = this.bankDictionary.getDescriptor(bankname).getProperty("banksize"); int nbytes = nwords*4; Integer nwords_i =
	 * nwords; int nwords_correct = nwords & 0x000000FF; //totalLength += nbytes; //int dataoffset = int dataindex = (bosBCS.getInt(header.NDATA_START_INDEX)-1)*4 +
	 * index.get(loop); int datalen = nbytes; totalLength += datalen; dataP.add(dataindex); dataL.add(datalen); //System.out.println("[GET BANK DATA] " + bankname +
	 * " " + index.get(loop) // + "  " + dataindex +"  " + datalen); } try { if(totalLength>0){ byte[] buffer = new byte[totalLength]; int start = 0; for(int loop =
	 * 0; loop < dataP.size(); loop++){ System.arraycopy(bosBCS.array(), dataP.get(loop), buffer, start, dataL.get(loop)); start += dataL.get(loop); } ByteBuffer
	 * ioBuffer = ByteBuffer.wrap(buffer); ioBuffer.order(ByteOrder.LITTLE_ENDIAN);
	 * 
	 * return ioBuffer; //return buffer; } } catch (Exception e) { System.out.print(" WELL Something went wrong with [" + bankname + "]  " );
	 * this.showBankInfo(bankname); this.dumpBufferToFile(); this.showBank(bankname, banknum); int index1 = this.getBankIndex(bankname, banknum, 0); int index2 =
	 * this.getBankIndex(bankname, banknum, index1+4); System.out.println("INDEX 1 = " + index1 + "  INDEX2 = " + index2); if(this.hasBank("HEAD")==true){
	 * BosDataBank bank = (BosDataBank) this.getBank("HEAD"); bank.show(); } for(int loop = 0; loop < dataP.size();loop++){ System.out.print( dataP.get(loop) + "   "
	 * + dataL.get(loop) + " " + "  INDEX = " + index.get(loop) + "   "); } System.out.println(); }
	 * 
	 * byte[] b = new byte[0]; ByteBuffer ioBuffer = ByteBuffer.wrap(b); ioBuffer.order(ByteOrder.LITTLE_ENDIAN); return ioBuffer; }
	 */
	public ArrayList<Integer> getBankMultiIndex(String bankname, int banknum) {
		ArrayList<Integer> index = new ArrayList<Integer>();
		int initial_pos = this.getBankIndex(bankname, banknum, 0);
		if (initial_pos >= 0) {
			index.add(initial_pos);
			while (initial_pos >= 0) {
				initial_pos = this.getBankIndex(bankname, banknum, initial_pos + 4);
				if (initial_pos >= 0)
					index.add(initial_pos);
			}
		}
		// System.out.println(" FOUND BANK [" + bankname +"] with positions = " + index.size());
		return index;
	}

	public DataBank getBank(String bank_name) {

		// System.out.println("---> Looking for " + bank_name +
		// " total size = " + bosBCS.array().length);
		String __bankname = bank_name;
		int __banknumber = 0;
		if (bank_name.contains(":") == true) {
			String[] tokens = bank_name.split(":");
			__bankname = tokens[0];
			__banknumber = Integer.parseInt(tokens[1]);
		}

		BosDataDescriptor desc = (BosDataDescriptor) bankDictionary.getDescriptor(__bankname);

		ByteBuffer bcsBank = this.getBankData(__bankname, __banknumber);
		// int index = this.getBankIndex(__bankname, __banknumber);
		// BosBankHeader header = new BosBankHeader(index);
		BosDataBank bank = new BosDataBank(desc);

		int dataoffset = 0;
		int banksize = desc.getProperty("banksize");
		int dataFirstByte = 0;
		int nrows = bcsBank.capacity() / banksize;
		/*
		 * System.out.println("[GETBANK:DEBUG] ----> " + bank_name + " index  = " + index + " offset = " + dataoffset + " rows = " + nrows + " cosl = " + ncols +
		 * " blen = " + bosBCS.array().length + " nwords = " + nwords + " bsize = " + banksize + " firstbyte = " + dataFirstByte);
		 */
		String[] entryNames = desc.getEntryList();
		for (String entry : entryNames) {
			if (desc.getProperty("type", entry) == 2) {
				int entry_offset = desc.getProperty("offset", entry);
				short[] short_data = new short[nrows];
				for (int loop = 0; loop < nrows; loop++) {

					int entry_index = entry_offset + loop * banksize + dataFirstByte;
					short_data[loop] = bcsBank.getShort(entry_index);
				}
				bank.setShort(entry, short_data);
			}

			if (desc.getProperty("type", entry) == 3) {
				int entry_offset = desc.getProperty("offset", entry);
				int[] int_data = new int[nrows];
				// short[] short_data = new short[nrows];
				// System.out.println("----> Filling entry " + entry + " size = "
				// + nrows + " index " + index + " first byte = " +
				// dataFirstByte + " offset = " + entry_offset);
				for (int loop = 0; loop < nrows; loop++) {

					int entry_index = entry_offset + loop * banksize + dataFirstByte;
					// System.out.println("[]--> filling entry int " + entry +
					// " offset = " + entry_offset +
					// " position " + entry_index + " loop = " + loop);
					// System.out.println("-------------> loop " + loop
					// + " banksize = " + banksize + " index = " + entry_index);
					int_data[loop] = bcsBank.getInt(entry_index);
				}
				bank.setInt(entry, int_data);
			}

			if (desc.getProperty("type", entry) == 5) {
				int entry_offset = desc.getProperty("offset", entry);
				float[] float_data = new float[nrows];
				// short[] short_data = new short[nrows];
				// System.out.println("----> Filling entry " + entry + " size = "
				// + nrows + " index " + index + " first byte = " +
				// dataFirstByte + " offset = " + entry_offset);
				for (int loop = 0; loop < nrows; loop++) {
					int entry_index = entry_offset + loop * banksize + dataFirstByte;
					/*
					 * System.out.println("[]--> filling entry float " + entry + " position " + entry_index + " loop = " + loop);
					 */
					float_data[loop] = bcsBank.getFloat(entry_index);
				}
				bank.setFloat(entry, float_data);
			}
		}

		// int rowlen =
		// int[] pid = new int[nrows];
		// int dataStartIndex = ;
		// for()
		return bank;
	}

	public DataBank getBankOldVersion(String bank_name) {

		// System.out.println("---> Looking for " + bank_name +
		// " total size = " + bosBCS.array().length);
		String __bankname = bank_name;
		int __banknumber = 0;
		if (bank_name.contains(":") == true) {
			String[] tokens = bank_name.split(":");
			__bankname = tokens[0];
			__banknumber = Integer.parseInt(tokens[1]);
		}
		BosDataDescriptor desc = (BosDataDescriptor) bankDictionary.getDescriptor(__bankname);

		int index = this.getBankIndex(__bankname, __banknumber);
		BosBankHeader header = new BosBankHeader(index);
		BosDataBank bank = new BosDataBank(desc);

		if (index > 0) {
			int nwords = bosBCS.getInt(header.NWORDS_WORD_INDEX);
			int nrows = bosBCS.getInt(header.NROWS_WORD_INDEX);
			int ncols = bosBCS.getInt(header.NCOLS_WORD_INDEX);

			// System.out.println("-----> found bank [] = " + bank_name
			// + " position = " + index + " words = " + nwords +
			// " nrows = " + nrows + " total size = " + bosBCS.array().length);
			int dataoffset = (bosBCS.getInt(header.NDATA_START_INDEX) - 1) * 4;
			int banksize = desc.getProperty("banksize");
			int dataFirstByte = index + dataoffset;
			/*
			 * System.out.println("[GETBANK:DEBUG] ----> " + bank_name + " index  = " + index + " offset = " + dataoffset + " rows = " + nrows + " cosl = " + ncols +
			 * " blen = " + bosBCS.array().length + " nwords = " + nwords + " bsize = " + banksize + " firstbyte = " + dataFirstByte);
			 */
			String[] entryNames = desc.getEntryList();
			for (String entry : entryNames) {
				if (desc.getProperty("type", entry) == 3) {
					int entry_offset = desc.getProperty("offset", entry);
					int[] int_data = new int[nrows];
					// short[] short_data = new short[nrows];
					// System.out.println("----> Filling entry " + entry + " size = "
					// + nrows + " index " + index + " first byte = " +
					// dataFirstByte + " offset = " + entry_offset);
					for (int loop = 0; loop < nrows; loop++) {

						int entry_index = entry_offset + loop * banksize + dataFirstByte;
						// System.out.println("[]--> filling entry int " + entry +
						// " offset = " + entry_offset +
						// " position " + entry_index + " loop = " + loop);
						// System.out.println("-------------> loop " + loop
						// + " banksize = " + banksize + " index = " + entry_index);
						int_data[loop] = bosBCS.getInt(entry_index);
					}
					bank.setInt(entry, int_data);
				}

				if (desc.getProperty("type", entry) == 5) {
					int entry_offset = desc.getProperty("offset", entry);
					float[] float_data = new float[nrows];
					// short[] short_data = new short[nrows];
					// System.out.println("----> Filling entry " + entry + " size = "
					// + nrows + " index " + index + " first byte = " +
					// dataFirstByte + " offset = " + entry_offset);
					for (int loop = 0; loop < nrows; loop++) {
						int entry_index = entry_offset + loop * banksize + dataFirstByte;
						/*
						 * System.out.println("[]--> filling entry float " + entry + " position " + entry_index + " loop = " + loop);
						 */
						float_data[loop] = bosBCS.getFloat(entry_index);
					}
					bank.setFloat(entry, float_data);
				}
			}
		}
		// int rowlen =
		// int[] pid = new int[nrows];
		// int dataStartIndex = ;
		// for()
		return bank;
	}

	public void getBank(String bank_name, DataBank bank) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public double[] getDouble(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setDouble(String path, double[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendDouble(String path, double[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public float[] getFloat(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setFloat(String path, float[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendFloat(String path, float[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int[] getInt(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setInt(String path, int[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendInt(String path, int[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public short[] getShort(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setShort(String path, short[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendShort(String path, short[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public ByteBuffer getEventBuffer() {
		return bosBCS;
	}

	public void setProperty(String property, String value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public String getProperty(String property) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public byte[] getByte(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setByte(String path, byte[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendByte(String path, byte[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendBanks(DataBank... bank) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setType(DataEventType type) {
		this.eventType = type;
	}

	public DataEventType getType() {
		return eventType;
	}

    public DataBank createBank(String bank_name, int rows) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void show() {
        System.out.println("[BosDataEvent]  show is not implemented");
    }

    @Override
    public void removeBank(String bankName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeBanks(String... bankNames) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long[] getLong(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void appendLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    

}
