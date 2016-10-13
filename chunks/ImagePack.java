import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class ImagePack {
	static BufferedWriter out;
	public static void main(String[] a) throws IOException {
		File folder = new File(".");
		File[] listOfFiles = folder.listFiles();
		out = new BufferedWriter(new FileWriter("output.txt"));
		System.out.print("Writing ");
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String s = listOfFiles[i].getName();
				if (s.contains(".png")){
					System.out.print(s+"...");
					octoConvert(listOfFiles[i]);
				}

			}
		}
		out.close();
		System.out.println();
		System.out.println("Nice! All files written to output.txt");
	}

	public static void octoConvert(File target) throws IOException{
		// handle arguments
		
		int spritew = 8;
		int spriteh = 1;
		
		Order order = Order.tblr;

		// load image data
		BufferedImage image = null;
		String basename = "";
		try {
			image = ImageIO.read(target);
			basename = target.getName().split("\\.")[0];
		}
		catch(IOException e) {
			System.err.println("unable to load image '"+target+"'");
			System.exit(1);
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
		List<Integer> palette = new ArrayList<Integer>();
		{
			palette.add(0xff2d1e2a);
			palette.add(0xff29565e);

		}

		// unpack image data
		List<Integer> data = new ArrayList<Integer>();
		int outerMax = order.getOuterMax(image, spritew, spriteh);
		int innerMax = order.getInnerMax(image, spritew, spriteh);
		List<HashSet<Integer>> planes = new ArrayList<HashSet<Integer>>();
		if (palette.size() > 2) {
			planes.add(new HashSet<Integer>(Arrays.asList(palette.get(1), palette.get(3))));
			planes.add(new HashSet<Integer>(Arrays.asList(palette.get(2), palette.get(3))));
		}
		else {
			planes.add(new HashSet<Integer>(Arrays.asList(palette.get(1))));
		}
		for(int outer = 0; outer < outerMax; outer++) {
			for(int inner = 0; inner < innerMax; inner++) {
				for(Set<Integer> plane : planes) {
					for(int yoff = 0; yoff < spriteh; yoff += 1) {
						for(int xoff = 0; xoff < spritew; xoff += 8) {
							data.add(order.getByte(outer, inner, spritew, spriteh, xoff, yoff, image, plane));
						}
					}
				}
			}
		}


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
		}
	}
}

enum Order {
	tblr(true ,true ,true ), // default
	tbrl(true ,true ,false),
	btlr(true ,false,true ),
	btrl(true ,false,false),
	lrtb(false,true ,true ),
	lrbt(false,true ,false),
	rltb(false,false,true ),
	rlbt(false,false,false);

	private final boolean verticalOuter;
	private final boolean ascendingOuter;
	private final boolean ascendingInner;

	private Order(boolean verticalOuter, boolean ascendingOuter, boolean ascendingInner) {
		this.verticalOuter  = verticalOuter;
		this.ascendingOuter = ascendingOuter;
		this.ascendingInner = ascendingInner;
	}

	public int getOuterMax(BufferedImage i, int spritew, int spriteh) {
		return  verticalOuter ? (i.getHeight()/spriteh) : (i.getWidth()/spritew);
	}

	public int getInnerMax(BufferedImage i, int spritew, int spriteh) {
		return !verticalOuter ? (i.getHeight()/spriteh) : (i.getWidth()/spritew);
	}

	public int getByte(int outer, int inner, int spritew, int spriteh, int xoff, int yoff, BufferedImage i, Set<Integer> colors) {
		if (!ascendingOuter) { outer = getOuterMax(i, spritew, spriteh) - 1 - outer; }
		if (!ascendingInner) { inner = getInnerMax(i, spritew, spriteh) - 1 - inner; }
		int x = xoff + ( verticalOuter ? (inner*spritew) : (outer*spritew));
		int y = yoff + (!verticalOuter ? (inner*spriteh) : (outer*spriteh));
		int ret = 0;
		for(int index = 0; index < 8; index++) {
			int pixel = i.getRGB(x + index, y);
			ret = ((ret << 1) | (colors.contains(pixel) ? 1 : 0));
		}
		return ret;
	}
}
