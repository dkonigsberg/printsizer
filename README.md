PrintSizer - Darkroom Printing Calculator
=========================================

PrintSizer is a darkroom printing calculator app for Android.

It allows you to quickly and easily determine the difference in print exposure
times when changing the height of your enlarger.

How Does it work?
-----------------
### Exposure Time Adjustment

To begin this process, you first need to input the following:
* Enlarger height (negative-to-print distance) for the first, or base, print.
* Exposure time for the first, or smaller, print.
* Enlarger height (negative-to-print distance) for the second, or target, print.
* Enlarger profile to use for calculating the new exposure.

Enlarger profiles, at a minimum, contain the focal length of the enlarging
lens and an optional height measurement offset.
The focal length is necessary to compute print magnification. The measurement
offset is a convenience feature that comes in handy if the height markings on
your enlarger do not directly correspond to the negative-to-print distance.

#### Without calibration

* Determines the magnification of the first, or base, print.
* Determines the magnification of the second, or target, print.
* Uses a well-known formula to calculate the change in exposure time between
  these two magnifications.

#### With calibration

* Determines the magnification of the first, or base, print.
* Determines the magnification of the second, or target, print.
* Calculates the exposure time for the first print, on the enlarger's
  reference curve.
* Calculates the exposure time for the second print, on the enlarger's
  reference curve.
* Uses the relative difference between the first exposure time and its
  position on the reference curve to calculate the second exposure time.

### Enlarger Calibration

#### Setting up the profile

To make an enlarger profile more accurate, you can add a set of two test
exposures to its profile. These should be height/time combos made on that
same enlarger, with the same lens and aperture, which produce identical
light-gray prints. Ideally, these two prints should be made at approximately
the smallest and largest sizes you intend to print.

If you have an enlarger meter, then you can skip the test printing process
and simply use two readings from that meter to build your profile.
It should also be possible to use an incident light meter, but your enlarger
lamp may be too dim to produce an accurate result via that method.

#### Computing the reference curve

The app uses the exposure height/time combos inputted above to interpolate a
reference curve that is used to calculate exposure changes. The process
involves attempting to create a curve, based on the test exposure times,
that closely matches the shape of an ideal reference curve. As the actual
process is complex and periodically refined, it is best to look at the
code in the "PrintMath" and "CalibratedEnlarger" classes to see how it works.

Reporting Issues
----------------
Issues and feature requests for this app can be reported via GitHub's issue
tracker for this repository:

https://github.com/dkonigsberg/printsizer/issues
