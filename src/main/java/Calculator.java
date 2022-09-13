import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Calculator {


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String[] oplocas= {"1+2+3+4+5",
                "(-1)+(-2)+(-3)+(3*4*5*6^0.5)-13/33",
                "(-1)+(-1)+(-2)+(-3)+(3*4*5*6^0.5)-13/33",
                "-1-1-1-2-2-2-3-3-3-4-4.5",
                "((+_seno(30)/+_coseno(30))/+_tangente(30))^+2",
                "(_seno(30)/_coseno(30))/_tangente(30)",
                "ln(300)",
                "ln(30)/65^(1/3)*ln(334)",
                "-(3.1415^3.1415)",
                "1+++++++1",
                "((_seno(3))^2)+(_coseno(3))^2"};
        for(int i=0;i<oplocas.length;i++) {
            System.out.println(oplocas[i]+" = "+String.valueOf(compute(oplocas[i])));
        }
    }

    private static boolean validateString(String str) {
        int par=0;
        String[] forbidden= {"+)","+^","+*","+/",
                "-)","-^","-*","-/",
                "()","(^","(*","(/",
                "^)","^^","^*","^/",
                "*)","*^","**","*/",
                "/)","/^","/*","//",};


        //Here we count if parenthesis are even
        //thus, there are many left parenthesis as right parenthesis
        for (int i=0;i<str.length();i++) {
            if (str.charAt(i)=='(') {
                par+=1;
            }else {
                if(str.charAt(i)==')') {
                    par-=1;
                }
            }
        }
        //Here we check if parenthesis are even
        if (par==0){
            //here we check if the string has a typo or an invalid sequence
            for (int j=0;j<forbidden.length;j++) {
                if (str.contains(forbidden[j])){
                    return false;
                }
            }
        }
        return true;
    }

    private static String correctString(String str) {
        //Putting missing multiplication symbols
        //We put multiplication symbols where they are needed
        //for example:
        // )( -> )*(
        String text=str.replaceAll("\\)\\(",")*(");
        String abc="abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNÑOPQRSTUVWXYZ1234567890";
        String aux,rem;

        //Change parenthesis-constant pre-multiplication:
        // )a -> )*a
        for (int i=0;i<abc.length();i++) {
            aux="\\)";
            rem="\\)\\*";
            aux+=abc.charAt(i);
            rem+=abc.charAt(i);
            text=text.replaceAll(aux,rem);
        }
        //Change parenthesis-constant post-multiplication:
        // a( -> a*(
        for (int i=0;i<abc.length();i++) {
            aux="\\(";
            rem="\\*\\(";
            aux=abc.charAt(i)+aux;
            rem=abc.charAt(i)+rem;
            text=text.replaceAll(aux,rem);
        }

        //Here characters are changed base on rules of signs
        text=text.replaceAll("\\-\\-","+")//--  -> +
                .replaceAll("\\-\\+","+-")// -+ -> +-
                .replaceAll("(?<![\\+])\\-","+-") // ++...+-  -> +-
                .replaceAll("\\+{2,}","+");//++ or ++....+ ->+

        //ESTA COSA SERVIRÁ MÁS TARDE para hacer split de los + que quiero "(?<![\\^/\\(\\*])\\+"
        //System.out.println("La cadena corregida es: "+text);
        return text;
    }

    private static float recursiveComputation(String str) {

        if(str.contains("(") || str.contains(")")){//str has parenthesis
            int contadorParentesis=0;
            //estos son los índices de la subcadena que se mandará de forma recursiva.
            int primerIndexPar=str.indexOf('(');
            int ultimoIndexPar=0;
            int aux=0;
            for(int i=0;i<str.length();i++){
                if (str.charAt(i)=='(') {
                    aux++;
                    contadorParentesis++;
                }else {
                    if(str.charAt(i)==')') {
                        contadorParentesis--;
                    }
                }
                if (contadorParentesis==0 && aux!=0) {
                    ultimoIndexPar=i;
                    i=str.length();
                }
            }
            String subCad=str.substring(primerIndexPar+1, ultimoIndexPar);
            //String origSubCad="";
            //origSubCad+=subCad;
            //System.out.println("Lo que está dentro del primer paréntesis: "+subCad);
            float valorPar= recursiveComputation(subCad);

            String[] operaciones= new String[] {str.substring(0,primerIndexPar),str.substring(ultimoIndexPar+1)};

            //aquí todavía no pongo cosas de funciones, pero si había
            //f(x) por aquí, consigo un f*+a, donde a es el valor numérico de x
            //eso lo permite el regex. Sólo debo aclarar que la multiplicación entre
            //una cierta cadena de caracteres y un numero en realidad es la evaluación
            //de la función en ese número
            System.out.println("BAndera: "+operaciones[0]);
            System.out.println("BAndera: "+operaciones[1]);


            String nuevaCad=operaciones[0]+"+"+String.valueOf(valorPar)+operaciones[1];

            System.out.println("Cadena antes de calculoRecursivo "+nuevaCad);
            if(nuevaCad.contains("(")||nuevaCad.contains(")")) {
                return recursiveComputation(nuevaCad);
            }else {//Aqui poner operaciones de nuevo
                return exitIteration(nuevaCad);

            }

        }else {
            return exitIteration(str);
        }

    }

    private static float exitIteration(String cad) {
        //esta es la condición de salida de la ecuación, aquí ya no hay paréntesis
        //separo la cadena sólo por símbolos de + fundamentales
        String cadena= correctString(cad);

        String[] operaciones1=cadena.split("(?<![\\^/\\(\\*])\\+");
        String[] operaciones2;
        String[] operaciones3;
        String[] operaciones4;
        float op4;
        float op3;
        float op2;
        float op1;

        String[] matchesDeRegex;
        String[] nomfunciones= {"_seno","_coseno","_tangente","aseno","acoseno","atangente","ln","exp"};
        String aux1="";
        String aux2="";
        String /*otras="",*/regex="",evaluar="";
        String parte1="",parte2="",contenido="";
        //Variables del regex
        Pattern pat;
        Matcher mat;
        for(int i=0;i<operaciones1.length;i++) {
            //aquí sí tengo que poner funciones, después debo poner exponenciación
            //en las funciones me aprovecho de que por estar antes entre paréntesis ya tengo sólo un resultado numérico
            for(int j=0;j<nomfunciones.length;j++) {

                while(operaciones1[i].contains(nomfunciones[j])) {
                    evaluar="";
                    regex="("+nomfunciones[j]+"\\*\\+\\-?[0-9]{0,}(\\.\\d{0,})?)";
                    //Aquí se comienza la búsqueda de Math por medio del regex
                    pat=Pattern.compile(regex);
                    mat=pat.matcher(operaciones1[i]);
                    while(mat.find()) {//este find no sólo valida, sino que ejecuta la búsqueda
                        evaluar+="#"+mat.group();
                        //System.out.println("El match del regex aquí "+evaluar);
                    }
                    matchesDeRegex=evaluar.split("#");//en el 0 no hay nada
                    //Por eso el for comienza en 1



                    parte1=operaciones1[i].substring(0,operaciones1[i].indexOf(matchesDeRegex[1]));
                    parte2=operaciones1[i].substring(operaciones1[i].indexOf(matchesDeRegex[1])+matchesDeRegex[1].length(),operaciones1[i].length());
                    //Aquí obtiene todo lo que no es el regex
                    //separaPorFun=operaciones1[i].split(regex);

                    //otras=operaciones1[i].substring(evaluar.length());
                    //System.out.println("Mira, estoy en la tele: "+otras);
                    //aquí debería tener el formato: fun*+
                    contenido=matchesDeRegex[1];
                    aux1=contenido.replace(nomfunciones[j]+"*+", "");
                    //System.out.println("separar: aux 12 "+aux1);
                    //ahora convierto de string a flotante, uso la función y
                    //luego de flotante a string
                    switch(nomfunciones[j]) {
                        case "_seno":
                            aux2=String.valueOf(Math.sin(Float.parseFloat(aux1)));
                            break;
                        case "_coseno":
                            aux2=String.valueOf(Math.cos(Float.parseFloat(aux1)));
                            break;
                        case "_tangente":
                            aux2=String.valueOf(Math.tan(Float.parseFloat(aux1)));
                            break;
                        case "aseno":
                            aux2=String.valueOf(Math.asin(Float.parseFloat(aux1)));
                            break;
                        case "acoseno":
                            aux2=String.valueOf(Math.acos(Float.parseFloat(aux1)));
                            break;
                        case "atangente":
                            aux2=String.valueOf(Math.atan(Float.parseFloat(aux1)));
                            break;
                        case "ln":
                            aux2=String.valueOf(Math.log(Float.parseFloat(aux1)));
                            //System.out.println("Adios: "+aux2);
                            break;
                        case "exp":
                            aux2=String.valueOf(Math.exp(Float.parseFloat(aux1)));
                            break;
                    }
                    //felicidades, ahora esto es un número

                    operaciones1[i]=parte1+aux2+parte2;
                    //System.out.println("estoy cansado: "+operaciones1[i]);
                }


            }

        }
        //bueno, las funciones ya están

        for(int i=0;i<operaciones1.length;i++) {
            //aquí tengo operaciones ^+ *+ y /+, y números negativos puros
            //ya me deshice de las funciones, tengo que poner las exponenciales ahora
            if(operaciones1[i].contains("*")) {//recordar que la cadena puede
                //tener varias exponenciales y otras operaciones dentro
                operaciones2=operaciones1[i].split("\\*\\+?");

                for(int j=0;j<operaciones2.length;j++) {
                    //recordar que multiplicación y división estan al mismo nivel
                    /////////////////////////////////////////////////
                    if(operaciones2[j].contains("/")) {
                        operaciones3=operaciones2[j].split("/\\+?");

                        //resultados3=new float[operaciones3.length];
                        for(int k=0;k<operaciones3.length;k++) {
                            op4=1;
                            if(operaciones3[k].contains("^")) {//hay multiplicaciones, divisiones y exponenciales
                                operaciones4=operaciones3[k].split("\\^\\+?");

                                //aquí transforma el primer valor a flotante
                                op4=Float.parseFloat(operaciones4[0]);
                                for(int m=1;m<operaciones4.length;m++) {
                                    op4=(float)Math.pow(op4,Float.parseFloat(operaciones4[m]));
                                }
                                operaciones3[k]=String.valueOf(op4);
                            }
                            //aquí op4 tiene el valor de las multiplicaciones

                        }

                        op3=Float.parseFloat(operaciones3[0]);
                        for(int k=1;k<operaciones3.length;k++) {
                            op3=op3/Float.parseFloat(operaciones3[k]);
                        }

                        operaciones2[j]=String.valueOf(op3);
                    }else{//hay multiplicaciones, exponenciales y no hay divisiones
                        if(operaciones2[j].contains("^")) {
                            operaciones3=operaciones2[j].split("\\^\\+?");
                            op3=Float.parseFloat(operaciones3[0]);
                            for(int k=1;k<operaciones3.length;k++) {
                                if(operaciones3[k].contains("+")) {
                                    op3=(float) Math.pow(op3,Float.parseFloat(operaciones3[k].substring(operaciones3[k].indexOf("+")+1)));
                                }else {
                                    op3=(float) Math.pow(op3,Float.parseFloat(operaciones3[k]));
                                }
                            }
                            operaciones2[j]=String.valueOf(op3);

                        }
                    }
                    ///////////////////////////////////
                } //a partir de aquí, operaciones2 tiene puros números
                System.out.println("Prueba: "+operaciones2[0]);
                op2=Float.parseFloat(operaciones2[0]);
                for(int j=1;j<operaciones2.length;j++) {
                    if(operaciones2[j].contains("+")) {
                        op2=op2*Float.parseFloat(operaciones2[j].substring(operaciones2[j].indexOf("+")+1));
                    }else {
                        op2=op2*Float.parseFloat(operaciones2[j]);
                    }

                }
                operaciones1[i]=String.valueOf(op2);


            }else {//aquí no hay multiplicaciones
                //con multiplicaciones y luego el de multiplicaciones solamente
                if(operaciones1[i].contains("/")) {
                    operaciones2=operaciones1[i].split("/\\+?");


                    for(int j=0;j<operaciones2.length;j++) {
                        if(operaciones2[j].contains("^")) {
                            operaciones3=operaciones2[j].split("\\^\\+?");
                            op3=Float.parseFloat(operaciones3[0]);
                            for(int k=1;k<operaciones3.length;k++) {
                                if(operaciones3[k].contains("+")) {
                                    op3=(float)Math.pow(op3,Float.parseFloat(operaciones3[k].substring(operaciones3[k].indexOf("+")+1)));
                                }else {
                                    op3=(float)Math.pow(op3,Float.parseFloat(operaciones3[k]));
                                }
                            }
                            operaciones2[j]=String.valueOf(op3);
                        }
                    }
                    op2=Float.parseFloat(operaciones2[0]);
                    for(int j=1;j<operaciones2.length;j++) {
                        if(operaciones2[j].contains("+")){
                            op2=op2/Float.parseFloat(operaciones2[j].substring(operaciones2[j].indexOf("+")+1));
                        }else {
                            op2=op2/Float.parseFloat(operaciones2[j]);
                        }
                    }
                    operaciones1[i]=String.valueOf(op2);
                    //System.out.println(operaciones1[i]);
                }else {//aquí no hay divisiones
                    if(operaciones1[i].contains("^")) {
                        operaciones2=operaciones1[i].split("\\^\\+?");
                        op2=Float.parseFloat(operaciones2[0]);
                        for(int j=1;j<operaciones2.length;j++) {
                            if(operaciones2[j].contains("+")) {
                                op2=(float)Math.pow(op2,Float.parseFloat(operaciones2[j].substring(operaciones2[j].indexOf("+")+1)));
                            }else {
                                op2=(float)Math.pow(op2,Float.parseFloat(operaciones2[j]));
                            }
                        }
                        operaciones1[i]=String.valueOf(op2);
                    }

                }

            }//aquí se terminan las operaciones, sigue la suma

            //System.out.println("operaciones1 "+i+" "+operaciones1[i]);

        }

        if(operaciones1[0].equals("")==false) {
            op1=Float.parseFloat(operaciones1[0]);
        }else {
            op1=0;
        }
        for(int i=1;i<operaciones1.length;i++) {
            //System.out.println("operaciones1 "+i+" "+operaciones1[i]);
            if(operaciones1[i].equals("")==false) {
                op1=op1+Float.parseFloat(operaciones1[i]);
            }
        }

        //System.out.println("Adios el valor de op1 es"+op1);
        return op1;

    }



    private static float compute(String str) {
        if (validateString(str)==false) {
            return 0;
        }else {//Here the istring is validated
            String text= correctString(str);

            return recursiveComputation(text);
        }

    }



    //si regresa -1, no encontró los símbolos
    private static int mayorIndice(String cadena, String[] simbolos) {
        int indiceMayor=-1;
        for(int i=0;i<cadena.length();i++) {
            for(int j=0;j<simbolos.length;j++) {
                if(cadena.charAt(i)==simbolos[j].charAt(0)) {
                    indiceMayor=(i>indiceMayor)?i:indiceMayor;
                }
            }
        }
        return indiceMayor;
    }

    //Si regresa un número igual a la longitud de la cadena, entonces
    //no se encontraron los símbolos en la cadena
    private static int minorIndex(String str, String[] simbols) {
        int minorInd=str.length();
        for(int i=0;i<str.length();i++) {
            for(int j=0;j<simbols.length;j++) {
                if(str.charAt(i)==simbols[j].charAt(0)) {
                    minorInd=(i<minorInd)?i:minorInd;
                }
            }
        }
        return minorInd;
    }


}
