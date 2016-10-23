import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class ImagePack {
	static BufferedWriter out;
	public static void main(String[] a) throws IOException {
		System.out.println();
		File folder = new File(".");
		File[] listOfFiles = folder.listFiles();
		out = new BufferedWriter(new FileWriter("output.txt"));
		for (int i = 0; i < listOfFiles.length; i++) {
			
			if (listOfFiles[i].isDirectory()) {
				File innerFolder = listOfFiles[i];
				if(innerFolder.getName().equals("unused")) continue;
				File[] innerList = innerFolder.listFiles();
				System.out.println("Writing folder: "+innerFolder.getName());
				System.out.println("----------------------");
				System.out.print("|");
				for (int j = 0; j<innerList.length;j++){
					String s = innerList[j].getName();
					if (s.contains(".png")){
						System.out.print(s+"|");
						octoConvert(innerFolder.getName(), innerList[j]);
					}	
				}	
				System.out.println();
				System.out.println("----------------------");
				System.out.println();
				

			}
		}
		out.close();
		System.out.println("Nice! All files written to output.txt");
	}

	public static void octoConvert(String folderName, File target) throws IOException{
		// handle arguments
		
		int spritew = 8;
		int spriteh = 1;
		
		// load image data
		BufferedImage image = null;
		String basename = "";
		try {
			image = ImageIO.read(target);
			basename = target.getName().split("\\.")[0];
		}
		catch(IOException e) {
			error("unable to load image '"+target+"'");
		}
		// check image dimensions
		if ((image.getWidth() % spritew) != 0 || (image.getHeight() % spriteh) != 0) {
			System.err.format("A %dx%d image is not evenly divisible into %dx%d sprites!%n",
				image.getWidth(),
				image.getHeight(),
				spritew,
				spriteh
				);
			System.exit(1);
		}

		// extract a palette.
		int backgroundColour = 0xff2d1e2a;
		int foregroundColour = 0xff29565e;
		ArrayList<Integer> specialColours = new ArrayList<Integer>();
		specialColours.add(0xffff0000);
		specialColours.add(0xffff0001);
		specialColours.add(0xffff0002);
		specialColours.add(0xffff0003);
		specialColours.add(0xffff0004);
		specialColours.add(0xffff0005);
		specialColours.add(0xffff0006);
		specialColours.add(0xffff0007);
		int data = 0;
		

		out.write(": "+folderName+"_"+basename);
		out.write("\n");
		out.write("\t"+image.getHeight());
		out.write("\n");
		out.write("\t");
		

		StringBuilder mainData = new StringBuilder();
		mainData.append("\n");
		mainData.append("\t");
		int[] metaData = new int[image.getHeight()/4];
		for(int i=0;i<metaData.length;i++){
			metaData[i]=-1;
		}
		for(int y=0;y<image.getHeight();y++){
			for(int x=0;x<image.getWidth();x++){
				data <<= 1;
				int pixel = image.getRGB(x, y);
				data += pixel==backgroundColour?0:1;
				if(pixel!=backgroundColour && pixel!=foregroundColour){
					if(!specialColours.contains(pixel)){
						error("invalid colour found: "+Integer.toHexString(pixel)+" in "+basename);
					}
					if(y%4 != 0){
						error("special colour found on non%4 y: "+y+" in "+basename);
					}
					if(x%4 != 0){
						error("special colour found on non%4 x: "+x+" in "+basename);
					}
					if(metaData[y/4]!=-1){
						error("multiple special colours found in row: "+y+" in "+basename);	
					}

					int meta = specialColours.indexOf(pixel);
					meta <<= 5;
					meta += x/4;
					metaData[y/4]=meta;

				}
				if(x%8==7){
					String string = Integer.toHexString(data);
					if(string.length()==1)string="0"+string;
					string = "0x"+string;
					mainData.append(string);
					if(x==image.getWidth()-1){
						mainData.append("\n");
						mainData.append("\t");
					}
					else{
						mainData.append(" ");
					}

					data = 0;
				}
			}
		}

		mainData.append("\n");
		for(int i:metaData){
			out.write((i==-1?0:i)+" ");
		}

		out.write(mainData.toString());
		/*

		// format out image data
		out.write(": chunk_"+basename);
		out.write("\n");
		out.write("\n");
		out.write("\t"+image.getHeight());
		out.write("\n");
		out.write("\t");
		
		
		for(int i=0;i<image.getHeight()/4;i++){
			out.write("0");
			if(i<image.getHeight()/4-1) out.write(" ");
		}
		out.write("\n");
		out.write("\t");
		for(int index = 0; index < data.size(); index++) {
			String hex = Integer.toHexString(data.get(index));
			if(hex.length()==1) hex = "0"+hex;
			hex = "0x"+hex;
			out.write(hex);

			if(index%8 == 7){
				out.write("\n\t");
			}
			else{
				out.write(" ");
			}
		}*/
	}
	static void error(String s){
		System.out.println();
		System.out.println();
		System.out.println("---------------------");
		if(Math.random()<.98){
			System.out.println("XxXxXx__ERROR__xXxXxX");
		}
		else{
			System.out.println("XxXxXx_Sephiroth_xXxXxX");
		}
		System.out.println("---------------------");
		System.out.println();
		System.out.println(s);
		System.exit(1);
	}
}

