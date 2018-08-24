/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bpnn;

import java.io.*;
import java.util.*;

/**
 *
 * @author DELL_2
 */
public class BPNN {
    static ArrayList<Float[ ]> filedata, data;
    static int layerLenght,numberSmple,epochNumber;
    static int[] neurons; 
    public static double n; 
    static double[][] input,target,sample,outputSample; 
    static double[] rawoutputSample; 
    static double[][][] wh,Acwht; 
    static double[][] Acth,DeltaN,op,th,xp; 	
    static float max_target,min_target;
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        layerLenght = 4;
        neurons = new int[]{4,10,8,1};
        n = 0.5d;
        numberSmple = 50;
        epochNumber = 5000;
        String inputFile = "./data/turbine.txt";
        
        filedata = readFile(inputFile);
        Float[][] dataArr = prepairData(filedata); 
        target = initializetarget(dataArr); 
        input = initializeinput(dataArr); 
        sample = initializesample(dataArr);
        Initialization();
        //Epochs
        int aleatoryPatt;
        for(int i =0; i <epochNumber; i++){
                for (int j =0;j<input.length;j++){

                        aleatoryPatt = (int)(input.length*Math.random());
                        backPropagation(input[aleatoryPatt], target[aleatoryPatt]);
                }
        }
	// Sample
        outputSample = new double[sample.length][10];
        for(int i=0;i<sample.length;i++){
                outputSample[i] = feedForward(sample[i]);
        }
        rawoutputSample = prepareOuput(outputSample);

        //OutPut FILE
        try {
                //Creating files
                File fileoput = new File("./data/Output.txt");
                File fileTh = new File("./data/threshold.txt");
                File fileWht = new File("./data/weight.txt");

                FileWriter fw = new FileWriter(fileoput.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                for(int i=0;i<rawoutputSample.length;i++){
                        bw.write(String.valueOf(rawoutputSample[i])+"\n");
                }
                bw.close();


                FileWriter fwThreshold = new FileWriter(fileTh.getAbsoluteFile());
                BufferedWriter bwThreshold = new BufferedWriter(fwThreshold);
                FileWriter fwWeight = new FileWriter(fileWht.getAbsoluteFile());
                BufferedWriter bwWeight = new BufferedWriter(fwWeight);
                for(int k=1;k<layerLenght;k++){
                        for(int i=0;i<neurons[k];i++){
                                bwThreshold.write(String.valueOf(th[k][i])+" --- ");
                                for(int j=0;j<neurons[k-1];j++){
                                        bwWeight.write(String.valueOf(wh[k][i][j])+" --- ");
                                }
                        }
                        bwThreshold.write("\n");
                        bwWeight.write("\n");
                }
                bwThreshold.close();
                bwWeight.close();
                System.out.println("Writed all output files");

        } catch (IOException e) {
                e.printStackTrace();
        }
    }
    public static ArrayList<Float[ ]> readFile(String inputFile) throws IOException{
		
		Scanner scanner = null;
		ArrayList<Float[]> dataMatrix = new ArrayList<>();
                int i,j;
		try {
			//String Lineatthemoment = null;
			scanner = new Scanner (new File(inputFile));
                        String regex = "(\\s)+";
                        for(i=0;i<4;i++){
                            String nextLine = scanner.nextLine();
                        }
                        
			while ( scanner.hasNext()) {
                            
                            Float number[] = new Float[5];
                            //String[] informationcolumn = Lineatthemoment.split("\\s+");
                            String[] row = scanner.nextLine().split(regex);
                            for(j=0;j<5;j++){
                               float datarow = Float.parseFloat(row[j]);
                               number[j] = datarow;
                               System.out.println(number[j]);
                            }
                            dataMatrix.add(number);					
					
			}

		} catch (IOException e) {
		} finally {
                    if (scanner != null)scanner.close();
		}
		return dataMatrix;
	}
    public static Float[][] prepairData(ArrayList<Float[ ]> filedata){
        List<Float[]> datatransf = new ArrayList<>(filedata.size());
            Float[] minValueArr = new Float[filedata.get(0).length];
            Float[] maxValueArr = new Float[filedata.get(0).length];
            for (int i = 0; i<filedata.get(0).length; i++){
                    Float[] tempinfo = new Float[filedata.size()];
                    for (int j = 0; j<filedata.size(); j++){
                            tempinfo[j] = filedata.get(j)[i];
                    }
                    Arrays.sort(tempinfo);
                    minValueArr[i] = tempinfo[0];
                    maxValueArr[i] = tempinfo[tempinfo.length - 1];
            }
            max_target = maxValueArr[filedata.get(0).length-1];
            min_target = minValueArr[filedata.get(0).length-1];
            for (int j = 0; j<filedata.size(); j++){
                    Float[] instanceinfo = new Float[filedata.get(0).length];
                    for (int i = 0; i<filedata.get(0).length; i++){
                            instanceinfo[i] = (float) ((filedata.get(j)[i] - minValueArr[i])/(maxValueArr[i] - minValueArr[i]));
                    }
                    datatransf.add(instanceinfo);
            }
           //return datatransf;


            Float[][] infoArr = new Float[datatransf.size()][datatransf.get(0).length];
            for(int i=0; i<datatransf.size();i++){
            System.arraycopy(datatransf.get(i), 0, infoArr[i], 0, datatransf.get(0).length);
            }
            return infoArr;
        
    }

    private static double[][] initializetarget(Float[][] dataArr) {
        double[][] auxarr = new double[dataArr.length - numberSmple][1];
        for(int i=0;i<dataArr.length - numberSmple; i++){
                auxarr[i][0] = dataArr[i][4];
        }
        return auxarr;
    }

    private static double[][] initializeinput(Float[][] dataArr) {
       double[][] auxarr = new double[dataArr.length - numberSmple][4];
        for(int i=0;i<dataArr.length - numberSmple; i++){
                for(int j=0;j<4;j++){
                        auxarr[i][j] = dataArr[i][j];
                }
        }
        return auxarr;
    }

    private static double[][] initializesample(Float[][] dataArr) {
        double[][] auxarr = new double[numberSmple][4];
        for(int i=0;i<numberSmple; i++){
                for(int j=0;j<4;j++){
                        auxarr[i][j] = dataArr[dataArr.length - numberSmple + i][j];
                }
        }
        return auxarr;
    }
    public static void Initialization(){
		
        th = new double[layerLenght][20];
        // Initial weight
        wh = new double[layerLenght][20][20];
        for(int k =1; k<layerLenght; k++){
                for(int i =0; i< neurons[k]; i++){
                        th[k][i] = Math.random() -0.5;
                        for(int j=0; j<neurons[k-1]; j++){
                                wh[k][i][j] = Math.random() -0.5;
                        }
                }
        }
        // Initial Oput array
        op = new double[layerLenght][20];
        xp = new double[layerLenght][20];
        DeltaN = new double[layerLenght][20];
        Acth = new double[layerLenght][20];
        Acwht = new double[layerLenght][20][20];
    }
    
    public static void backPropagation(double[] onePattern, double auxarr[]){
	
        for(int i =0; i<onePattern.length; i++){
                op[0][i] = onePattern[i]; 
        }

        for(int k=1; k<layerLenght; k++){
                for(int i=0; i<neurons[k]; i++){
                        xp[k][i] = -th[k][i];				
                        for(int j =0;j<neurons[k-1];j++){
                                xp[k][i] += op[k-1][j]*wh[k][i][j];
                        }
                        op[k][i] = sigmoid(xp[k][i]);
                }
        }

        for(int i=0;i<neurons[layerLenght-1];i++){
                DeltaN[layerLenght-1][i] = op[layerLenght-1][i]*(1-op[layerLenght-1][i])*(op[layerLenght-1][i]-auxarr[i]);
        }
        for(int k=layerLenght-1;k>1;k--){
                for(int j=0;j<neurons[k-1];j++){
                        double summ = 0;
                        for(int i=0;i<neurons[k];i++){
                                summ += DeltaN[k][i]*wh[k][i][j];
                        }
                        DeltaN[k-1][j] = op[k-1][j]*(1-op[k-1][j])*summ;
                }
        }

        for(int k =layerLenght-1;k>0;k--){
                for(int i=0;i<neurons[k];i++){
                        for(int j=0;j<neurons[k-1];j++){
                                Acwht[k][i][j] = -n*DeltaN[k][i]*op[k-1][j];
                        }
                }
        }

        for(int k = layerLenght-1;k>0;k--){
                for(int i=0;i<neurons[k];i++){
                        Acth[k][i] = n*DeltaN[k][i];
                }
        }

        for(int k=1; k<layerLenght;k++){
                for(int i=0;i<neurons[k];i++){
                        for(int j=0;j<neurons[k-1];j++){
                                wh[k][i][j] +=Acwht[k][i][j];
                        }
                        th[k][i] += Acth[k][i];
                }
        }
    }
    public static double[] feedForward(double[] onePattern){
        double[] auxarr = new double[20];
        for(int i =0; i<onePattern.length; i++){
                op[0][i] = onePattern[i]; 
        }

        for(int k=1; k<layerLenght; k++){
            for(int i=0; i<neurons[k]; i++){
                    xp[k][i] =-th[k][i];
                    for(int j =0;j<neurons[k-1];j++){
                            xp[k][i] += op[k-1][j]*wh[k][i][j];
                    }
                    op[k][i] = sigmoid(xp[k][i]);
            }
        }

        for(int i=0;i<neurons[layerLenght-1];i++){
                auxarr[i] = op[layerLenght-1][i];
        }
        return auxarr;
    }
    
    public static double[] prepareOuput(double[][] outputSample){
        double[] rawOutSample = new double[outputSample.length];
        for(int i=0;i<outputSample.length;i++){
                rawOutSample[i] = outputSample[i][0]*((double)(max_target-min_target))+(double)(min_target);
        }
        return rawOutSample;
    }
    
    public static double sigmoid(double num){
        double res = 0;
        if (num>50){res = 1;}
        else if (num<-50){res = 0;}
        else{res = 1 / (1 + Math.exp(-num));}
        return res;
    }
}
