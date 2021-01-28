package android.os;
 
/** {@hide} */
interface IWiegandService
{
	int setReadFormat(int format);
	int setWriteFormat(int format);
	int read();
	int write(int data);
}

