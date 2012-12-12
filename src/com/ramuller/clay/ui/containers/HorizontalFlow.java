package com.ramuller.clay.ui.containers;

import com.ramuller.clay.ui.Component;
import com.ramuller.clay.ui.Container;

public class HorizontalFlow extends Container {

	public HorizontalFlow(Component parent) {
		super(parent);
	}
	private class HorizLayoutInfo extends LayoutInfo{
		/**
		 * Is this component's size fixed?
		 */
		private boolean fixed;
		/**
		 * Component's relative weight
		 */
		private float weight;
		
		public HorizLayoutInfo(Component c){
			super(c);
			fixed = true;
			weight=1;
		}
		public HorizLayoutInfo(Component c,float w){
			super(c);
			fixed = false;
			weight = w;
		}
		
		public boolean isFixed() {
			return fixed;
			// TODO Auto-generated constructor stub
		}
		public void setFixed(boolean fixed) {
			this.fixed = fixed;
		}
		public float getWeight() {
			return weight;
		}
		public void setWeight(float weight) {
			this.weight = weight;
		}
		
	}
	@Override
	public void reflow() {
		int x = 0;
		Component c;
		float xw=0;

		for(LayoutInfo li:getLayoutList()){
			xw += ((HorizLayoutInfo)li).getWeight();
		}
		xw /= getLayoutList().size();
		for(LayoutInfo li:getLayoutList()){
			c = li.getComponent();
			c.setHeight(getHeight());
			c.setY(0);
			c.setX(x);
			c.setWidth((int)(xw*(((HorizLayoutInfo)li).getWeight())));
		}
	}

}
