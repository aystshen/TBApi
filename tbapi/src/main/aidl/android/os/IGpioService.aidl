package android.os;

/** {@hide} */
interface IGpioService
{
    int gpioWrite(int gpio, int value);
    int gpioRead(int gpio);
    int gpioDirection(int gpio, int direction, int value);
    int gpioRegKeyEvent(int gpio);
    int gpioUnregKeyEvent(int gpio);
    int gpioGetNumber();
}